import cv2
import os

def split_video_to_frames(video_path, output_folder):
    # Create output folder if it doesn't exist
    os.makedirs(output_folder, exist_ok=True)

    # Open the video file
    video_capture = cv2.VideoCapture(video_path)

    # Get video information
    fps = int(video_capture.get(cv2.CAP_PROP_FPS))
    frame_count = int(video_capture.get(cv2.CAP_PROP_FRAME_COUNT))

    # Loop through each frame and save it as a grayscale image
    for frame_number in range(frame_count):
        ret, frame = video_capture.read()
        if not ret:
            break

        # Convert frame to grayscale
        #gray_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

        # Save grayscale frame as an image
        frame_path = os.path.join(output_folder, f"none{frame_number:04d}.png")
        cv2.imwrite(frame_path, frame)

    # Release the video capture object
    video_capture.release()

if __name__ == "__main__":
    # Replace 'your_video.mp4' with the path to your video file
    video_path = 'none.mp4'

    # Specify the output folder
    output_folder = 'Data/none'

    split_video_to_frames(video_path, output_folder)
    print(f"Grayscale frames extracted and saved in the '{output_folder}' folder.")
