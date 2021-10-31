name: Bug Report
description: File a bug report
title: "[Bug]: "
labels: ["bug"]
assignees:
- dementisimus
  body:
- type: markdown
  attributes:
  value: |
  Thanks for taking the time to fill out this bug report!
- type: textarea
  id: expected_behavior
  attributes:
  label: Expected Behavior
  description: Tell us what should happen
  placeholder: 
  validations:
  required: true
- type: textarea
  id: current_behavior
  attributes:
  label: Current Behavior
  description: Tell us what happens instead of the expected behavior
  placeholder:
  validations:
  required: true
- type: textarea
  id: steps_to_reproduce
  attributes:
  label: Steps to Reproduce
  description: How can we reproduce this bug?
  placeholder:
  validations:
  required: true
- type: textarea
  id: detailed_description
  attributes:
  label: Detailed Description
  description: Describe the bug detailed
  placeholder:
  validations:
  required: true
- type: input
  id: java_version
  attributes:
  label: Your Java version
  description: What version of Java are you using?
  placeholder:
  validations:
  required: true
- type: input
  id: minecraft_version
  attributes:
  label: Your Minecraft version
  description: What version of Minecraft are you using?
  placeholder:
  validations:
  required: true
- type: input
  id: is_paper
  attributes:
  label: Paper usage
  description: Are you using PaperMC?
  placeholder:
  validations:
  required: true
- type: input
  id: mapcreator_version
  attributes:
  label: MapCreator version
  description: What version of MapCreator are you using?
  placeholder:
  validations:
  required: true
