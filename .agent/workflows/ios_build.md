---
description: Build and Deploy iOS App
---

# 1. Build Framework for Simulator (Intel Mac)
# Run this if you are using the iOS Simulator on an Intel Mac
./gradlew :shared:linkDebugFrameworkIosX64

# 2. Build Framework for Device (iPhone/iPad)
# Run this if you are deploying to a physical device
./gradlew :shared:linkDebugFrameworkIosArm64

# 3. Link Resources (Fix Crash)
# Run this if you added new images/resources and they are missing
cd iosApp && bundle exec ruby link_resources.rb
