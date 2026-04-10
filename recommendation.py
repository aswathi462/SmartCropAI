from fastapi import FastAPI
import json

app = FastAPI()

# Load recommendation JSON file once
with open("disease_recommendations.json") as f:
    REC_DATA = json.load(f)


@app.get("/")
def home():
    return {"message": "API is running!"}


# ----------- FETCH RECOMMENDATION ---------------
@app.get("/recommendation/{disease_name}")
def get_recommendation(disease_name: str):
    # convert name: "Brown Spot" -> "brown_spot"
    key = disease_name.lower().replace(" ", "_")

    if key not in REC_DATA:
        return {"error": "Recommendation not found"}

    return {
        "disease": disease_name,
        "recommendation": REC_DATA[key]
    }