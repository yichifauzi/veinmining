The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

This is a copy of the changelog for the most recent version. For the full version history, go [here](https://github.com/illusivesoulworks/veinmining/blob/1.20.x/CHANGELOG.md).

## [4.0.0+1.20.6] - 2024.05.10
### Changed
- Updated to Minecraft 1.20.6
- Updated to SpectreLib 0.16.1
- Changed `rarity` configuration option to `weight` and `anvilCost`
- Expanded `minEnchantabilityBase` and `minEnchantabilityPerLevel` configuration options into `minCostBase`, `minCostPerLevel`,
  `maxCostBase`, and `maxCostPerLevel`
- [Fabric] Changed default items to use vanilla tags instead of modded tags
### Removed
- Removed `groupsList` configuration option
