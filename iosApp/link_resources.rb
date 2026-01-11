require 'xcodeproj'

project_path = 'iosApp.xcodeproj'
project = Xcodeproj::Project.open(project_path)

# Path to the composeResources folder relative to the project
resources_path = '../shared/src/commonMain/composeResources'
group_name = 'composeResources'

# Get the main group
main_group = project.main_group

# Check if the reference already exists
# We iterate to find if any child matches the path to be sure
existing_ref = main_group.children.find { |child| child.path == resources_path }

if existing_ref
  puts "Resource folder already linked."
else
  puts "Linking composeResources..."
  # Add the folder reference
  # Using new_reference with directory path acts as folder reference
  new_ref = main_group.new_reference(resources_path)
  new_ref.name = group_name
  new_ref.last_known_file_type = 'folder'
  
  # Find the main target (usually the first one, or 'iosApp')
  target = project.targets.find { |t| t.name == 'iosApp' } || project.targets.first
  
  if target
    # Add to the "Copy Bundle Resources" build phase
    resources_phase = target.resources_build_phase
    
    # Check if it's already in the build phase to avoid duplicates
    unless resources_phase.files_references.include?(new_ref)
        build_file = resources_phase.add_file_reference(new_ref)
        puts "Added verification: #{build_file.display_name} to #{target.name}"
    end
    
    project.save
    puts "Successfully linked composeResources to #{target.name}."
  else
    puts "Error: Could not find target 'iosApp'"
    exit 1
  end
end
