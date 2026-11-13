$java = "java"
if (Test-Path "C:\Program Files\Java\jre-1.8\bin\java.exe") {
    $java = "C:\Program Files\Java\jre-1.8\bin\java.exe"
} elseif (Test-Path "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe") {
    $java = "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.2\jbr\bin\java.exe"
}

& $java -cp "bin;lib\mysql-connector-j-8.3.0.jar;src" com.inventory.Main

