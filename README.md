# Tasky Backend v2.0
  ### Ktor Server for [Tasky (Task Management) Android App](https://github.com/realityexpander/Tasky)

## Overview
Tasky is a reminder app that allows users to create, manage, and receive reminders for events. 
The backend server is built using Ktor, a Kotlin framework for building asynchronous servers and clients in connected systems.
The server is designed to handle user authentication, task management, and event reminders.

### Built With
- Kotlin 2.1.2
- Ktor 3.1.3 server application
- Koin 4.0.4 for dependency injection
- MongoDB for data storage
- S3 for file storage (Images)
- Tasky Android Application Source: https://github.com/realityexpander/Tasky
- All secret keys and credentials are stored in a separate configuration file for security purposes.
- Demonstrates the use of Koin for dependency injection, Ktor for server-side development, and MongoDB for data storage.
  - includes a sample API key generation and validation process, as well as a sample task management system with reminders and attendees.
  - Server uses JWT and refresh tokens for authentication and authorization.
  - All API endpoints are secured with JWT authentication.
  - Ability to kill tokens for security purposes.
  - Uses HOCON for configuration management.

## Setup:
- Clone this repository
- Get FREE S3 bucket credentials from iDrive e2 (https://www.idrive.com/) 
- Get FREE MongoDB Atlas credentials from MongoDB Atlas (https://cloud.mongodb.com/)
- Set up environment variables in `authenticationSecrets.conf` in the `src/main/resources` directory
  - Refer to the sample [authenticationSecrets.conf.example](src/main/resources/authenticationSecrets.conf.example) file for the required variables.
  - Make sure to rename the file to `authenticationSecrets.conf` after editing.
- Set up the database in MongoDB Atlas
  - Create the `tasky` database.
  - Create the following collections:
    - `user`
    - `apiKey`
    - `task`
    - `event`
    - `reminder`
    - `attendee`
    - `killedToken`
- Run Server Locally
  - Use the command `./gradlew run` to start the server. (Or just run from IDE)
  - The server will be available at `http://localhost:8080`.

## Coming Soon: 
  - Instructions for deploying the server to VPS

## Development Notes
  - IDE: IntelliJ IDEA
    - ```kotlin
      IntelliJ IDEA 2025.1.1.1 (Community Edition)
      Build #IC-251.25410.129, built on May 9, 2025
      Source revision: b815cfdcaa594
      Runtime version: 21.0.6+9-b895.109 aarch64 (JCEF 122.1.9)
      VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
      Toolkit: sun.lwawt.macosx.LWCToolkit
      macOS 15.4.1
      Kotlin plugin: K2 mode
      GC: G1 Young Generation, G1 Concurrent GC, G1 Old Generation
      Memory: 3048M
      Cores: 10
      Metal Rendering is ON
      Registry:
      ide.experimental.ui=true
      completion.cloud.enableTextCompletion=false
      llm.show.ai.promotion.window.on.start=false
      kotlin.scripting.index.dependencies.sources=true
      Non-Bundled Plugins:
      com.jetbrains.space (251.23774.318)
      com.jetbrains.edu (2025.4-2025.1-530)
      org.jetbrains.settingsRepository (251.25410.28)
      wu.seal.tool.jsontokotlin (3.7.6)
      com.github.DerekBum.composeSlidesPresenter (0.1.2)
      DBN (3.5.3.0)
      net.ashald.envfile (3.4.2)
      org.jetbrains.plugins.hocon (2025.1.0)
      com.intellij.mermaid (0.0.25+IJ.243)
      com.github.fisherman08.Idea-WebSocket (1.0.1)
      com.wakatime.intellij.plugin (15.0.3)
      com.intellij.ml.llm (251.23774.42.28.7)
      com.intellij.grazie.pro (0.3.377)
      org.jetbrains.plugins.gitlab (251.25410.159-IU)
      org.jetbrains.plugins.github (251.25410.159-IU)
      com.google.CoroutineStacks (1.0.3)
      org.jetbrains.compose.desktop.ide (1.7.3)
      org.jetbrains.android (251.25410.131)
      com.eric-li.layout-inspector-v2 (1.0.6)
      androidx.compose.plugins.idea (251.23774.318)
      com.github.copilot (1.5.45-243)
      com.jetbrains.writerside (2025.04.8412)
      mobi.hsz.idea.gitignore (4.5.6)
      com.intellij.java.rareRefactorings (251.23774.318)
      Kotlin: 251.25410.129-IJ

  - ENVIRONMENT VARIABLES
    - `AWS_JAVA_V1_DISABLE_DEPRECATION_ANNOUNCEMENT=true` - This is to disable the Java 1.x use deprecation warning for AWS SDK (S3 bucket)

  ### Java Version used:
      java --version                                                                                                                                                                                                                                                                       1 ↵  1144  14:08:36 
      openjdk 21.0.6 2025-01-21
      OpenJDK Runtime Environment (build 21.0.6+-13391695-b895.109)
      OpenJDK 64-Bit Server VM (build 21.0.6+-13391695-b895.109, mixed mode)


## My Environment Notes:
- Credentials Saved in LastPass as Note

  ### S3 Bucket Alternative - iDrive e2 - (realityexpanderdev@gmail.com)
      - https://app.idrivee2.com/region/LA/buckets/tasky/object-storage?prefix=%2F
      - Bucket: `tasky.w1d1.la5.idrivee2-10.com`
    
      - Developer Guide: 
        - https://www.idrive.com/s3-storage-e2/developer-guide
  ### Local S3 Bucket - Minio
  - Installation: https://min.io/open-source/download
    - Startup (CLI): `minio server /Volumes/TRS-83/data`
    - Dashboard: http://127.0.0.1:63009/browser
    - CLI to configure:

    ```
    aws configure set aws_access_key_id <access-key-id>
    aws configure set aws_secret_access_key <secret-access-key>
    aws s3api list-buckets --endpoint-url http://127.0.0.1:9000
    aws s3api create-bucket --bucket tasky --endpoint-url http://127.0.0.1:9000      
    ```
### AWS CLI docs: 
  - https://docs.aws.amazon.com/cli/v1/userguide/cli-configure-envvars.html

### MongoDB Atlas
  - MongoDB Atlas (realityexpanderdev@gmail.com)
    - https://cloud.mongodb.com/v2/6822725603bd487e26487991#/metrics/replicaSet/682272b8fb8c845215074f78/explorer/tasky/reminder/find
      - Database: `Tasky-1 @ AWS Oregon (us-west-2)`

### Version History
- Version 0.0.7 - Uses Configuration Files
- Version 0.0.8 - Fixes Image Uploading, support multiple S3 services


























































