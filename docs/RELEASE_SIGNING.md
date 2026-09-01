# Android release signing

No signing key, keystore, certificate, password, alias, or production API URL belongs in Git. Debug APK output is not a release candidate.

## Required release environment

Release Gradle tasks require an HTTPS `API_BASE_URL` plus:

- `ALLERGENRADAR_RELEASE_STORE_FILE`
- `ALLERGENRADAR_RELEASE_STORE_PASSWORD`
- `ALLERGENRADAR_RELEASE_KEY_ALIAS`
- `ALLERGENRADAR_RELEASE_KEY_PASSWORD`

The build script rejects blank values, HTTP, localhost, `10.0.2.2`, and the example host. Keep the keystore in the approved secret system or secured build runner; pass it ephemerally to the build, then delete the workspace copy.

## Validation sequence

1. Run unit tests, lint, and Debug assembly.
2. In a controlled release environment, run `./gradlew assembleRelease -PAPI_BASE_URL=https://approved.example/`.
3. Verify the release APK with the matching Android SDK `apksigner verify --verbose` command.
4. Record the version, commit SHA, artifact digest, signer identity (not secret material), verifier output, and approver externally.

Until this has occurred with real controlled credentials, release signing status remains `BLOCKED_EXPECTED`.
