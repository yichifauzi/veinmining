The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/illusivesoulworks/veinmining/blob/1.21.x/CHANGELOG.md).

## [5.0.0-beta+1.21] - 2024.07.02
### Added
- Added datapack method for specifying block groups
- Added `enableEnchantmentWarnings`, `enableEnchantmentTooltips`, and `enableEnchantmentNotifications` configuration
  fields to `veinmining-client.toml` to better guide users about potential usage and errors
### Changed
- Updated to Minecraft 1.21
- Migrated the enchantment configuration from `veinmining-common.toml` to a datapack file
  `veinmining:enchantment/vein_mining.json`
- Expanded configuration comments for activation state fields
### Removed
- Removed `veinmining-common.toml` and its associated configuration values
