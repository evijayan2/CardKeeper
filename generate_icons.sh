#!/bin/bash
SOURCE="shared/src/commonMain/composeResources/drawable/ic_app_logo_1.png"
DEST="iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

# 20pt
sips -z 40 40 "$SOURCE" --out "$DEST/Icon-App-20x20@2x.png"
sips -z 60 60 "$SOURCE" --out "$DEST/Icon-App-20x20@3x.png"

# 29pt
sips -z 58 58 "$SOURCE" --out "$DEST/Icon-App-29x29@2x.png"
sips -z 87 87 "$SOURCE" --out "$DEST/Icon-App-29x29@3x.png"

# 40pt
sips -z 80 80 "$SOURCE" --out "$DEST/Icon-App-40x40@2x.png"
sips -z 120 120 "$SOURCE" --out "$DEST/Icon-App-40x40@3x.png"

# 60pt
sips -z 120 120 "$SOURCE" --out "$DEST/Icon-App-60x60@2x.png"
sips -z 180 180 "$SOURCE" --out "$DEST/Icon-App-60x60@3x.png"

# 1024pt
sips -z 1024 1024 "$SOURCE" --out "$DEST/Icon-App-1024x1024.png"
