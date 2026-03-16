import pandas as pd

# Load dataset
data = pd.read_csv("crop_dataset.csv")


# Create yield column using a formula
data["Yield"] = (
    data["Nitrogen"] * 0.02 +
    data["Phosphorus"] * 0.015 +
    data["Potassium"] * 0.02 +
    data["Temperature"] * 0.05 +
    data["Humidity"] * 0.01 +
    data["Rainfall"] * 0.005
) / 10

# Save new dataset
data.to_csv("crop_dataset_with_yield.csv", index=False)

print("Yield column added successfully!")