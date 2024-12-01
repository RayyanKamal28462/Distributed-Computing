<?php
// Database configuration
$servername = "localhost";
$port = 3307; // Your MySQL port
$username = "root"; // Your MySQL username
$password = "ryynkml22"; // Your MySQL password
$dbname = "step_tracker"; // Your MySQL database name

// Validate and sanitize input data
$errors = [];

function sanitizeInput($data) {
    return htmlspecialchars(stripslashes(trim($data)));
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    // Check if all required fields are present
    if (!isset($_POST['height'], $_POST['weight'], $_POST['steps'], $_POST['duration'])) {
        $errors[] = "All fields are required.";
    } else {
        // Sanitize input data
        $height = sanitizeInput($_POST['height']);
        $weight = sanitizeInput($_POST['weight']);
        $steps = sanitizeInput($_POST['steps']);
        $duration = sanitizeInput($_POST['duration']);

        // Validate numeric values
        if (!is_numeric($height) || !is_numeric($weight) || !is_numeric($steps) || !is_numeric($duration)) {
            $errors[] = "Invalid input data.";
        }

        // Connect to MySQL database
        try {
            $conn = new PDO("mysql:host=$servername;port=$port;dbname=$dbname", $username, $password);
            $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

            // Prepare SQL statement for insertion
            $sql = "INSERT INTO step_data (height, weight, steps, duration, calories) VALUES (:height, :weight, :steps, :duration, :calories)";
            $stmt = $conn->prepare($sql);

            // Calculate calories burned
            $calories = (3.5 * 8 * $weight * $duration) / 200;

            // Bind parameters and execute
            $stmt->bindParam(':height', $height);
            $stmt->bindParam(':weight', $weight);
            $stmt->bindParam(':steps', $steps);
            $stmt->bindParam(':duration', $duration);
            $stmt->bindParam(':calories', $calories);
            $stmt->execute();

            // Close connection
            $conn = null;

            // Redirect to results page or handle response
            header("Location: results.php");
            exit();
        } catch (PDOException $e) {
            echo "Error: " . $e->getMessage();
        }
    }
}
?>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Submit Form</title>
    <link rel="stylesheet" href="https://www.w3schools.com/w3css/4/w3.css">
    <style>
        .error {
            color: red;
        }
    </style>
</head>
<body>
    <div class="w3-container w3-teal">
        <h2 class="w3-center">Step Tracker Submission</h2>
    </div>

    <div class="w3-container">
        <?php if (!empty($errors)): ?>
            <div class="w3-panel w3-pale-red w3-border">
                <p><strong>Error:</strong></p>
                <ul>
                    <?php foreach ($errors as $error): ?>
                        <li><?php echo $error; ?></li>
                    <?php endforeach; ?>
                </ul>
            </div>
        <?php endif; ?>

        <div class="w3-card-4 w3-light-grey w3-margin">
            <div class="w3-container w3-teal">
                <h2 class="w3-center">Submit Steps</h2>
            </div>

            <form class="w3-container" method="POST" action="<?php echo htmlspecialchars($_SERVER["PHP_SELF"]); ?>">
                <label class="w3-text-teal">Height (in meters)</label>
                <input class="w3-input w3-border" type="number" step="0.01" name="height" required>

                <label class="w3-text-teal">Weight (in kilograms)</label>
                <input class="w3-input w3-border" type="number" step="0.1" name="weight" required>

                <label class="w3-text-teal">Steps</label>
                <input class="w3-input w3-border" type="number" name="steps" required>

                <label class="w3-text-teal">Duration (in minutes)</label>
                <input class="w3-input w3-border" type="number" name="duration" required>

                <button class="w3-button w3-block w3-teal w3-section w3-padding" type="submit">Submit</button>
            </form>
        </div>
    </div>

    <footer class="w3-container w3-center w3-teal">
        <p>StepTracker&copy;</p>
    </footer>
</body>
</html>
