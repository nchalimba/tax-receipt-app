# EASY TAXFOX

## Used Technologies

Android SDK, SQLite, Android Room, Runnables, Google Vision API

## Description

The app makes managing receipts for the tax declaration more accessible. The end user kann manage, and import the relevant receipts, as well as export the metadata and the files in a pdf file. This file contains all receipts in a given time period.

## Features

Thie application has a dashboard, in which the user can get an overview of the collected receipts.

![DashboardActivity](https://user-images.githubusercontent.com/64647045/112602619-37b48680-8e14-11eb-997a-a47d02bdb6d0.png)

On top of that, the user can navigate to the overview-, profile-, settings- and export-page.

![BurgerMenu](https://user-images.githubusercontent.com/64647045/112602851-7e09e580-8e14-11eb-91eb-9d5f161a929e.png)

In order to insert a new receipt, the user needs to grant the following permissions to the application:

- Access to contacts
- Access to images
- Access to the storage
- Access to the internet

After granting the permissions, the user needs to specify a Google account for the Google Vision API. After that, the user can import the receipt and crop it.

![Gallery](https://user-images.githubusercontent.com/64647045/112603313-1f913700-8e15-11eb-8759-d7b50e8149c7.png) ![Cropper](https://user-images.githubusercontent.com/64647045/112603432-48193100-8e15-11eb-91b0-da1c1ad936d7.png)

After the insertion of the receipt, the user will be redirected to a form.

![ReceiptDataActivity](https://user-images.githubusercontent.com/64647045/112603669-9d554280-8e15-11eb-8b81-91f622bc30cd.png)

Ther, the metadata of the receipt can be inserted. The Google Vision API predicts values for the title and the total amount.
On top of that, a preview of the scan is shown in the bottom of the page. The user now has the possibility to save or cancel the receipt. After that, the overview page appears, in which all receipts are shown.

![OverviewActivity](https://user-images.githubusercontent.com/64647045/112603991-fd4be900-8e15-11eb-9658-09a3e929e5cd.png)

In the profile page, the user can specify his/her first and last name. When the user has not yet specified that data, he will automatically be redirected to this page while starting the app.

![ProfileActivity](https://user-images.githubusercontent.com/64647045/112604163-24a2b600-8e16-11eb-9eb2-151b50a79e2b.png)

In the settings page, the user can specify the business year and the theme of the app (light / dark mode). On top of that, the Google Vision API can be enabled / disabled. This data is saved in the shared preferences of the application

![SettingsActivityLightMode](https://user-images.githubusercontent.com/64647045/112604337-4dc34680-8e16-11eb-8f6b-5dabc19667bf.png) ![SettingsActivityNightMode](https://user-images.githubusercontent.com/64647045/112604436-6b90ab80-8e16-11eb-9327-76061587f705.png)

In the settings page, the user can create a pdf-file out of the receipts. For this functionality, the user must specify, whether the application should create a single file containing all receipts or one file for every receipt. Further, a time period must be specified.

<img width="314" alt="ExportActivity" src="https://user-images.githubusercontent.com/64647045/112726509-222b8380-8f1e-11eb-925f-3aa95359fb47.png">

After clicking the download button, the file can be shared or viewed.

![Share](https://user-images.githubusercontent.com/64647045/112726524-2d7eaf00-8f1e-11eb-9f00-09d306568400.png)

## Google Vision API

The Google Cloud Vision API is used to predict a title and the total amount of a scanned receipt. To map the JSON data from the API into actual predictions, the following Concept was implemented: https://blog.mi.hdm-stuttgart.de/index.php/2019/08/31/using-googles-cloud-vision-api-to-create-a-receipt-analyzer/
![Google Vision API](https://user-images.githubusercontent.com/64647045/112605663-d68eb200-8e17-11eb-84ce-74b5026af2ac.png)

## Libraries:

- iText 7: https://mvnrepository.com/artifact/com.itextpdf/itext7-core/7.1.3
- PDFBox Android: https://github.com/TomRoush/PdfBox-Android
- ScanLibrary: https://github.com/jhansireddy/AndroidScannerDemo
- MPAndroidChart: https://github.com/PhilJay/MPAndroidChart
- Google Cloud Vision API: https://cloud.google.com/vision/docs/quickstart-client-libraries

#### App Logo:

Attribution for the logo: https://www.flaticon.com/free-icon/fox_4065958?related_id=4065958&origin=search
