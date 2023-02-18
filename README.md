# 750-words-importer

Utility to import journal entries from 750words.com to org-journal, optionally encrypting them with GPG.

## Usage

Export all your entries from 750words.com, one month at a time, and put the resulting .txt files in the `input` directory. Then run the utility. Parsed org entries will be written to `output`.

750 recently launched a new website that is in beta. The export format for the new site is slightly different, so there is some crude conversion code in the `convert-new-entry` function.

## Encryption

To have each entry encrypted with GPG, generate a key with `gpg --full-generate-key` and update `gpg-recipient` with your email. Each entry will then be encrypted.


## Questions?

If something is not working, create a Github issue and I'll be happy to help sorting it out.
