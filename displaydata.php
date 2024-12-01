<!DOCTYPE html>
<html>
<head>
    <title>Display Data</title>
    <style>
        table {
            width: 50%;
            margin: 20px auto;
            border-collapse: collapse;
        }
        table, th, td {
            border: 1px solid black;
            padding: 8px;
            text-align: left;
        }
        th {
            background-color: #f2f2f2;
        }
    </style>
</head>
<body>

<h2 style="text-align: center;">Step Tracker Data</h2>

<table>
    <tr>
        <th>ID</th>
        <th>Height</th>
        <th>Weight</th>
        <th>Steps</th>
        <th>Duration</th>
        <th>Calories</th>
    </tr>

    <?php
    // Database connection (example using PDO)
    $host = 'localhost';
    $db = 'step_tracker';
    $user = 'root';
    $pass = 'ryynkml22';

    try {
        $pdo = new PDO("mysql:host=$host;dbname=$db", $user, $pass);
        $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);

        // Retrieve data from database
        $stmt = $pdo->query("SELECT * FROM step_data");
        while ($row = $stmt->fetch()) {
            echo "<tr>";
            echo "<td>" . $row['id'] . "</td>";
            echo "<td>" . $row['height'] . "</td>";
            echo "<td>" . $row['weight'] . "</td>";
            echo "<td>" . $row['steps'] . "</td>";
            echo "<td>" . $row['duration'] . "</td>";
            echo "<td>" . $row['calories'] . "</td>";
            echo "</tr>";
        }
    } catch (PDOException $e) {
        echo "Error: " . $e->getMessage();
    }
    ?>
</table>

</body>
</html>
