import cv2
import os

def image_binary(input_path, output_path, output_suffix="vid3"):
    image = cv2.imread(input_path)

    # Check if the image is loaded successfully
    if image is None:
        print(f"Error: Unable to load image at {input_path}")
        return
    height, width = image.shape[:2]
    cut_percentage = 0.9
    cut_height = int(height * cut_percentage)
    cut_image = image[:cut_height, :]

    cv2.destroyAllWindows()

    # Get the original filename without extension
    file_name, file_extension = os.path.splitext(os.path.basename(input_path))

    # Save the processed image with a modified filename
    output_image_path = os.path.join(output_path, file_name + output_suffix + file_extension)
    cv2.imwrite(output_image_path, cut_image)

if __name__ == "__main__":
    input_folder = "Data/bil"
    output_folder = "Data/Stop_start"

    # Ensure the output folder exists
    os.makedirs(output_folder, exist_ok=True)

    # Process all images in the input folder
    for filename in os.listdir(input_folder):
        if filename.endswith(".png") or filename.endswith(".jpg"):
            input_path = os.path.join(input_folder, filename)
            image_binary(input_path, output_folder)
