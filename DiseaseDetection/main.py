from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.responses import JSONResponse
import uvicorn
import numpy as np
from PIL import Image
import firebase_admin
from firebase_admin import credentials, firestore
from pydantic import BaseModel
import json
import os
import io
import cv2
import torch
import torch.nn.functional as F
import timm
import torch.nn as nn
import albumentations as A
from albumentations.pytorch import ToTensorV2

app = FastAPI()


class SignUpRequest(BaseModel):
    full_name: str
    email: str
    password: str

class LoginRequest(BaseModel):
    email: str
    password: str


cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()


REC_DATA = {}
if os.path.exists("disease_recommendations.json"):
    with open("disease_recommendations.json", "r") as f:
        REC_DATA = json.load(f)
else:
    print("Warning: disease_recommendations.json not found.")


DEVICE = torch.device("cuda" if torch.cuda.is_available() else "cpu")

with open("model_config.json") as f:
    config = json.load(f)

CLASSES  = config['classes']
IMG_SIZE = config['img_size']

class DiseaseClassifier(nn.Module):
    def __init__(self, model_name, num_classes, pretrained=False, dropout=0.4):
        super().__init__()
        self.backbone = timm.create_model(
            model_name, pretrained=pretrained,
            num_classes=0, global_pool='avg'
        )
        in_features = self.backbone.num_features
        self.head = nn.Sequential(
            nn.LayerNorm(in_features),
            nn.Dropout(dropout),
            nn.Linear(in_features, 512),
            nn.GELU(),
            nn.Dropout(dropout / 2),
            nn.Linear(512, num_classes)
        )
    def forward(self, x):
        return self.head(self.backbone(x))

model = DiseaseClassifier(config['model_id'], config['num_classes'])
model.load_state_dict(torch.load("efficientnetv2_s_best.pth", map_location=DEVICE))
model.to(DEVICE)
model.eval()

transforms = A.Compose([
    A.Resize(height=IMG_SIZE, width=IMG_SIZE),
    A.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
    ToTensorV2(),
])


def is_rice_leaf(img_bytes):
    """Check if the image is likely a rice leaf before disease prediction."""
    img_np = np.array(Image.open(io.BytesIO(img_bytes)).convert('RGB'))
    img_bgr = cv2.cvtColor(img_np, cv2.COLOR_RGB2BGR)
    img_hsv = cv2.cvtColor(img_bgr, cv2.COLOR_BGR2HSV)

 
    lower_green = np.array([25, 30, 30])
    upper_green = np.array([95, 255, 255])

    green_mask = cv2.inRange(img_hsv, lower_green, upper_green)
    green_ratio = np.sum(green_mask > 0) / green_mask.size

    # At least 15% of image must be green
    return green_ratio >= 0.15

def predict_disease(img_bytes):
    img_np = np.array(Image.open(io.BytesIO(img_bytes)).convert('RGB'))
    tensor = transforms(image=img_np)['image'].unsqueeze(0).to(DEVICE)
    with torch.no_grad():
        probs = F.softmax(model(tensor), dim=1)[0].cpu().numpy()
    pred_class = CLASSES[probs.argmax()]
    confidence = round(float(probs.max()) * 100, 2)
    return pred_class, confidence


@app.get("/")
def home():
    return {"message": "API is running!"}

@app.post("/upload_leaf")
async def upload_leaf(image: UploadFile = File(...)):
    contents = await image.read()

  
    if not is_rice_leaf(contents):
        return JSONResponse(
            status_code=400,
            content={
                "status": "Rejected",
                "message": "Not a rice leaf. Please upload a clear image of a rice plant leaf."
            }
        )


    disease_name, certainty = predict_disease(contents)

   
    if disease_name.lower() == "healthy":
        treatment_details = {
            "treatments": ["No treatment needed. Your crop looks healthy!"],
            "preventive": ["Maintain regular field hygiene and proper drainage."],
            "fertilizer": ["Continue with your current balanced NPK schedule."]
        }
    else:
        lookup_key = disease_name.lower().replace(" ", "_")
        treatment_details = REC_DATA.get(
            lookup_key,
            {
                "treatments": ["No specific treatment found in records."],
                "preventive": ["Maintain overall field sanitation and clean water drainage."],
                "fertilizer": ["Follow custom soil-test-based NPK guidelines."]
            }
        )

    doc_ref = db.collection("diagnoses").document()
    doc_ref.set({
        "disease": disease_name,
        "confidence": f"{certainty}%",
        "timestamp": firestore.SERVER_TIMESTAMP,
        "location": "Kerala"
    })

    
    return {
        "status": "Success",
        "diagnosis": disease_name,
        "confidence": f"{certainty}%",
        "firebase_id": doc_ref.id,
        "recommendation": treatment_details
    }

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

    existing = list(db.collection("users").where("email", "==", email).limit(1).stream())
    if existing:
        raise HTTPException(status_code=409, detail="Email is already registered")

    doc_ref = db.collection("users").document()
    doc_ref.set({
        "full_name": full_name,
        "email": email,
        "password": password,
        "timestamp": firestore.SERVER_TIMESTAMP,
    })

    return {"status": "success", "message": "Signup successful",
            "full_name": full_name, "email": email}

@app.post("/auth/login")
async def auth_login(payload: LoginRequest):
    email = payload.email.strip().lower()
    password = payload.password

    if "@" not in email or not password:
        raise HTTPException(status_code=400, detail="Email and password are required")

    user_docs = list(db.collection("users").where("email", "==", email).limit(1).stream())
    if not user_docs:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    user_data = user_docs[0].to_dict() or {}
    if user_data.get("password") != password:
        raise HTTPException(status_code=401, detail="Invalid email or password")

    return {"status": "success", "message": "Login successful",
            "full_name": user_data.get("full_name", "User"), "email": email}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)