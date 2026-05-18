from fastapi import FastAPI, UploadFile, File, HTTPException
import uvicorn
import cv2
import numpy as np
import predictor
import firebase_admin
from firebase_admin import credentials, firestore
from pydantic import BaseModel
import json
import os

app = FastAPI()

# --- DATA MODELS FOR AUTHENTICATION ---
class SignUpRequest(BaseModel):
    full_name: str
    email: str
    password: str


class LoginRequest(BaseModel):
    email: str
    password: str


# --- INITIALIZE FIREBASE CONFIGURATION ---
# Connect to Firebase using the local key file (ignored by git for security)
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()


# --- LOAD TREATMENT MODULE ON STARTUP ---
# Loading this into memory once prevents reading the file on every API request
REC_DATA = {}
JSON_FILENAME = "disease_recommendations.json"

if os.path.exists(JSON_FILENAME):
    with open(JSON_FILENAME, "r") as f:
        REC_DATA = json.load(f)
else:
    print(f"Warning: '{JSON_FILENAME}' not found. Default recommendations will be applied.")


@app.get("/")
def home():
    return {"message": "API is running!"}


# ----------- MERGED CORE AI & TREATMENT ENDPOINT ---------------
@app.post("/upload_leaf")
async def upload_leaf(image: UploadFile = File(...)):
    # 1. Read the image stream sent via Android Retrofit multipart request
    contents = await image.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        raise HTTPException(status_code=400, detail="Invalid image file uploaded.")

    # 2. Get AI Prediction (Executes shared MobileNetV2 preprocessing & inference)
    disease_name, certainty = predictor.detect_disease(img)
    
    # 3. Dynamic Treatment Mapping
    # Standardize name for JSON keys (e.g., "Brown Spot" -> "brown_spot")
    lookup_key = disease_name.lower().replace(" ", "_")
    
    # Retrieve matching treatments; provide fallback default block if disease isn't mapped
    treatment_details = REC_DATA.get(
        lookup_key, 
        {
            "treatments": ["No specific chemical treatment found in records."],
            "preventive": ["Maintain overall field sanitation and clean water drainage."],
            "fertilizer": ["Follow custom soil-test-based NPK guidelines."]
        }
    )
    
    # 4. Save Record to Firebase Firestore Database
    doc_ref = db.collection("diagnoses").document()
    doc_ref.set({
        "disease": disease_name,
        "confidence": f"{certainty}%",
        "timestamp": firestore.SERVER_TIMESTAMP,
        "location": "Kerala"
    })

    # 5. Combined Response back to Retrofit Client
    return {
        "status": "Success", 
        "diagnosis": disease_name,
        "confidence": f"{certainty}%",
        "firebase_id": doc_ref.id,
        "recommendation": treatment_details
    }


# ----------- AUTHENTICATION MODULES ---------------
@app.post("/auth/signup")
async def auth_signup(payload: SignUpRequest):
    full_name = payload.full_name.strip()
    email = payload.email.strip().lower()
    password = payload.password

    if not full_name:
        raise HTTPException(status_code=400, detail="Full name is required")

    if "@" not in email:
        raise HTTPException(status_code=400, detail="Invalid email")

    if len(password) < 4:
        raise HTTPException(status_code=400, detail="Password must be at least 4 characters")

    existing_user_docs = list(
        db.collection("users").where("email", "==", email).limit(1).stream()
    )
    if existing_user_docs:
        raise HTTPException(status_code=409, detail="Email is already registered")

    doc_ref = db.collection("users").document()
    doc_ref.set({
        "full_name": full_name,
        "email": email,
        "password": password,
        "timestamp": firestore.SERVER_TIMESTAMP,
    })

    return {
        "status": "success",
        "message": "Signup successful",
        "full_name": full_name,
        "email": email,
    }


@app.post("/auth/login")
async def auth_login(payload: LoginRequest):
    email = payload.email.strip().lower()
    password = payload.password

    if "@" not in email or not password:
        raise HTTPException(status_code=400, detail="Email and password are required")

    user_docs = list(
        db.collection("users").where("email", "==", email).limit(1).stream()
    )
    if not user_docs:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    user_data = user_docs[0].to_dict() or {}
    stored_password = user_data.get("password")

    if stored_password != password:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    return {
        "status": "success",
        "message": "Login successful",
        "full_name": user_data.get("full_name", "User"),
        "email": email,
    }


if __name__ == "__main__":
    # Start the application server locally on Port 8001
    uvicorn.run(app, host="0.0.0.0", port=8001)