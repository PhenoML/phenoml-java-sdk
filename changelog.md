## v0.0.35 - 2025-11-17
* chore: update User-Agent header version placeholder
* The User-Agent header in ClientOptions has been updated to use a generic version placeholder instead of a hardcoded version string. This change prepares the SDK for automated version management during the build process.
* Key changes:
* Replace hardcoded version "v0.0.34" with "AUTO" placeholder in User-Agent header
* Maintain consistent SDK identification format
* Enable dynamic version injection during build/release process
* 🌿 Generated with Fern

