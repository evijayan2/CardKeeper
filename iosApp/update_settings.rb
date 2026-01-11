require 'xcodeproj'

project_path = 'iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)
target = project.targets.find { |t| t.name == 'iosApp' } || project.targets.first

if target
    target.build_configurations.each do |config|
        config.build_settings['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'AppIcon'
    end
    project.save
    puts "Updated build settings to use AppIcon for target #{target.name}."
else
    puts "Error: Could not find target 'iosApp'"
    exit 1
end
