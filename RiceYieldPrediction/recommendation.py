# recommendation.py

def generate_recommendation(N, P, K, ph, rain, temp=None, hum=None):
    advice = []

    # NITROGEN
    if N < 50:
        advice.append("Nitrogen is low (<50 kg/ha): Apply urea or organic manure.")
    elif 50 <= N <= 90:
        advice.append("Nitrogen level is optimal (50–90 kg/ha).")
    else:
        advice.append("Nitrogen is high (>90 kg/ha): Reduce nitrogen application to prevent lodging.")

    # PHOSPHORUS
    if P < 30:
        advice.append("Phosphorus is low (<30 kg/ha): Apply DAP or rock phosphate.")
    elif 30 <= P <= 45:
        advice.append("Phosphorus level is optimal (30–45 kg/ha).")
    else:
        advice.append("Phosphorus is high (>45 kg/ha): Excess phosphate may cause imbalance.")

    # POTASSIUM
    if K < 30:
        advice.append("Potassium is low (<30 kg/ha): Apply potash fertilizer (MOP).")
    elif 30 <= K <= 50:
        advice.append("Potassium level is optimal (30–50 kg/ha).")
    else:
        advice.append("Potassium is high (>50 kg/ha): Avoid over-application.")

    # SOIL pH
    if ph < 5.5:
        advice.append("Soil is acidic (pH < 5.5): Apply lime.")
    elif 5.5 <= ph <= 7.5:
        advice.append("Soil pH is optimal (5.5–7.5).")
    else:
        advice.append("Soil is alkaline (pH > 7.5): Apply sulfur or organic matter.")

    # RAINFALL
    if rain < 150:
        advice.append("Rainfall is low (<150 mm): Provide supplementary irrigation.")
    elif 150 <= rain <= 300:
        advice.append("Rainfall is adequate (150–300 mm).")
    else:
        advice.append("Excess rainfall (>300 mm): Improve drainage.")

    # TEMPERATURE
    if temp is not None:
        if temp < 20:
            advice.append("Temperature is low (<20°C): Use cold-tolerant varieties.")
        elif 20 <= temp <= 35:
            advice.append("Temperature is optimal (20–35°C).")
        else:
            advice.append("High temperature (>35°C): Increase irrigation to reduce heat stress.")

    # HUMIDITY
    if hum is not None:
        if hum < 50:
            advice.append("Humidity is low (<50%): Increase irrigation.")
        elif 50 <= hum <= 90:
            advice.append("Humidity is optimal (50–90%).")
        else:
            advice.append("High humidity (>90%): High risk of fungal diseases.")

    advice.append("Use certified seeds, proper spacing, and organic matter for improved soil health.")

    return advice