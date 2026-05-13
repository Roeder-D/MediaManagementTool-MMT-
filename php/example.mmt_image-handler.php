<?php
$SECRET_TOKEN = "your_secure_fixed_token_here";

// Check for valid Authorization header
$headers = apache_request_headers();
$authHeader = $headers['Authorization'] ?? '';

if($authHeader !== "Bearer " . $SECRET_TOKEN){
    header('HTTP/1.1 403 Forbidden');
    echo json_encode(["status" => "error", "message" => "Invalid or missing token"]);
    exit;
}

// Upload
if($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_FILES['image'])){
    $targetDir = "mmt-cover_images/";
    $fileName = basename($_POST['filename']);
    $targetFile = $targetDir . $fileName;

    $check = getimagesize($_FILES['image']['tmp_name']);
    if($check === false){
        echo json_encode(['status' => "error", "message" => "File is not an image."]);
        exit;
    }

    if(move_uploaded_file($_FILES['image']['tmp_name'], $targetFile)){
        echo json_encode([
            "status" => "success",
            "message" => "Image upload successfull",
            "filename" => $fileName
        ]);
    }else{
        header('HTTP/1.1 500 Internal Server Error');
        echo json_encode(["status" => "error", "message" => "No file or invalid request"]);
    }
}
//DELETE
else if($_SERVER['REQUEST_METHOD'] === 'POST' && isset($_GET['action']) && $_GET['action'] === 'delete'){
    $fileName = basename($_GET['filename']);
    $targetFile = "mmt-cover_images/" . $fileName;
    if(file_exists($targetFile)){
        unlink($targetFile);
        echo json_encode(["status" => "success", "message" => "File deleted"]);
    } else {
        echo json_encode(["status" => "error", "message" => "File not found"]);
    }
    exit;
}else{
    echo json_encode(["status" => "error", "message" => "No file or invalid request"]);
}
?>