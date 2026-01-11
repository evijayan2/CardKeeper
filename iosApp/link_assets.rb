require 'xcodeproj'

project_path = 'iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Path to the Assets.xcassets folder relative to the project root (iosApp folder)
assets_path = 'iosApp/Assets.xcassets'
group_name = 'Assets' # or just add it to 'iosApp' group

# Get the main group
main_group = project.main_group

# We want to add it to the 'iosApp' group (which corresponds to 'iosApp' folder usually)
ios_app_group = main_group.children.find { |child| child.display_name == 'iosApp' && child.path == 'iosApp' }

if !ios_app_group
    puts "Could not find 'iosApp' group. Adding to main group."
    ios_app_group = main_group
end

# Check if the reference already exists
existing_ref = ios_app_group.children.find { |child| child.path == 'Assets.xcassets' }

if existing_ref
  puts "Assets.xcassets already linked."
else
  puts "Linking Assets.xcassets..."
  # Add the folder reference
  # For xcassets, we usually add it as a file reference (it treats the .xcassets bundle as a file)
  new_ref = ios_app_group.new_reference('Assets.xcassets')
  
  # Find the main target
  target = project.targets.find { |t| t.name == 'iosApp' } || project.targets.first
  
  if target
    # Add to the "Copy Bundle Resources" build phase
    resources_phase = target.resources_build_phase
    
    unless resources_phase.files_references.include?(new_ref)
        build_file = resources_phase.add_file_reference(new_ref)
        puts "Added verification: #{build_file.display_name} to #{target.name}"
    end
    
    project.save
    puts "Successfully linked Assets.xcassets to #{target.name}."

    # Update Build Settings to point to AppIcon
    target.build_configurations.each do |config|
        config.build_settings['ASSETCATALOG_COMPILER_APPICON_NAME'] = 'AppIcon'
        # Also ensure it knows where to look for assets? usually automatic if file is linked
    end
    project.save
    puts "Updated build settings to use AppIcon."
  else
    puts "Error: Could not find target 'iosApp'"
    exit 1
  end
end
