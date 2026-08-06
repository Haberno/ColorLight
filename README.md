# ColorLight

**ColorLight / ColorLight with Shaders / Vanilla**

![Mod Enable / ColorLight With Shader / Vanilla](https://cdn.modrinth.com/data/cached_images/54a6e293b9c6827ac9da645298fb0942abb07dc1_0.webp)

ColorLight is an attempt to bring colored lighting support to a wide range of mods and resource packs without relying on a predefined list of light-emitting blocks, while still allowing users to customize that list manually.

ColorLight works with all shaders, whether they already support colored lighting or not. The mod is also compatible with most optimization mods, although there are some exceptions. You can find more information or report compatibility issues on the [GitHub issues page](https://github.com/MrHikmen/ColorLight/issues).

ColorLight can also be used for map making and building projects, as it allows you to create custom light sources using commands. Multiplayer support is planned, allowing custom light source data to be synchronized with other players.

### How It Works

When the mod detects a light-emitting block, it:

* Finds the block's texture.
* Analyzes the texture.
* Determines its dominant color (currently with varying accuracy).
* Uses that color as the block's light color.

### Configuration

All current mod settings are available through **Sodium**.

The **General** tab allows you to:

* Enable or disable the mod.
* Enable predefined lighting (a built-in lighting configuration for vanilla blocks only).
* Adjust the light radius.
* Adjust the lighting balance.

The **Blocks** tab allows you to:

* Enable or disable lighting for individual blocks.
* Set a custom light radius for each block.
* Customize the light color for each block.

### Supported Versions

| Version | Supported | Porting Status |
| :--- | :---: | :---: |
| - 1.20.1 | ✅ | In Progress |
| - 1.21.1 | ✅ | Completed |
| - 1.21.2–3| ❌ | Not Planned |
| - 1.21.4 | ❌ | Not Planned |
| - 1.21.5 | ❌ | Not Planned |
| - 1.21.6–8 | ❌ | Not Planned |
| - 1.21.9–10 | ❌ | Not Planned |
| - 1.21.11 | ✅ | Completed |
| - 26.1.x | ✅ | Completed |
| - 26.2 | ✅ | Completed |

<details>
<summary>Rus</summary>


# ColorLight
**ColorLight / ColorLight С Шейдерами / Ванилла**

![Mod Enable / ColorLight With Shader / Vanilla](https://cdn.modrinth.com/data/cached_images/54a6e293b9c6827ac9da645298fb0942abb07dc1_0.webp)

ColorLight — это попытка реализовать цветное освещение для огромного количества модов и ресурспаков без использования заранее заданного списка светящихся блоков, при этом со
хранив возможность настраивать этот список вручную.

ColorLight работает со всеми шейдерами и с теми, у которых нет своего цветного освещения, и с теми, у которых есть цветное освещение. Так же мод функционирует с оптимизирующими модами, но есть исключения о них вы можете узнать или сами написать на [GitHub issues page](https://github.com/MrHikmen/ColorLight/issues).

ColorLight может использоваться для строительства карта или построек, так как у мода есть возможность создавать источники света в ручную через команды, вскоре мод сможет работать и в мультиплеере передавая данные о новых источниках света другим игрокам.

### Как работает мод

При обнаружении светящегося блока мод:

* Находит текстуру блока
* Анализирует её
* Определяет преобладающий цвет (пока успех этого относителен)
* Использует этот цвет для освечения блока

### Настройки

Все настройки для мода сейчас находятся в **Sodium**.

Вкладка *"Основные"* отвечает за:

* Включение мода
* Включение настроеного освещение (список уже готового освещения только для ванильных блоков)
* Регулировка радиуса свечения
* Регулиравка баланса освещения

Вкладка *"Блоки"* отвечает за:

* Влючение отдельного блока
* Регулировка радиуса освещение отдельная для блока
* Регулировки цвета свечения отдельные для блока

### Команды



### Поддерживаемые Версии

| Версия | Поддержка | Перенос |
| :--- | :---: | :---: |
| - 1.20.1 | ✅ | В разработке |
| - 1.21.1 | ✅ | Завершен |
| - 1.21.2-3 | ❌ | Не будет |
| - 1.21.4 | ❌ | Не будет |
| - 1.21.5 | ❌ | Не будет |
| - 1.21.6-8 | ❌ | Не будет |
| - 1.21.9-10 | ❌ | Не будет |
| - 1.21.11 | ✅ | Завершен |
| - 26.1.x | ✅ | Завершен |
| - 26.2 | ✅ | Завершен |

</details>