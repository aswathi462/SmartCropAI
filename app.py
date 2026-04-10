from fastapi import FastAPI, UploadFile, File
import uvicorn
import cv2
import numpy as np
import predictor
import firebase_admin
from firebase_admin import credentials, firestore

app = FastAPI()

# Connect to Firebase using the file you just downloaded
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

@app.post("/upload_leaf")
async def upload_leaf(image: UploadFile = File(...)):
    # 1. Read the image
    contents = await image.read()
    nparr = np.frombuffer(contents, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    # 2. Get AI Prediction (includes shared preprocessing)
    disease_name, certainty = predictor.detect_disease(img)
    
    # 4. Save to Firebase Database
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
        "firebase_id": doc_ref.id
    }

if __name__ == "__main__":
    # Start the server on Port 8000
    uvicorn.run(app, host="0.0.0.0", port=8000)