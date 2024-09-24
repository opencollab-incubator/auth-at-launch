Auth at Launch
===

A mod which intercepts the Minecraft client launch and if the client isn't authenticated
(e.g. started in a dev environment) it will pop up and attempt to log in.

The idea that this project will be look after multiple auth needs and can be plugged in within:
- Mod toolchains (mod developers can easily get an authenticated client during development)
- Modpacks (rapid local testing or alternative distribution methods)
- Mod developers (inclusion in local mod development environments as a utility mod)

Currently you can either run the program directly and get the relevant command line args to add to a Minecraft client 
launch, or alternatively you can include the built jar as a mod and 

## Current State
This is a pre-alpha incubator project. It is not recommended for external use at this time

## Todo
- More resilient error handling
- Automatic versioning and releasing
- Distribution to Maven and mod distribution platforms
- Documentation
