$env:JAVA_HOME = Join-Path $env:TEMP 'smart-task-manager-jdk17\jdk-17.0.18+8'
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/postgres'
$env:DATABASE_USERNAME = 'postgres'
$env:DATABASE_PASSWORD = 'khan'
$env:SERVER_PORT = '8081'
$env:SEED_DEMO_DATA = 'true'
& .\gradlew.bat bootRun --no-daemon '-Dorg.gradle.jvmargs=-Xmx192m -Xms32m -XX:MaxMetaspaceSize=96m' '-Dspring-boot.run.jvmArguments=-Xmx160m -Xms32m'
