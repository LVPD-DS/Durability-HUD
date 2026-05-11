# Durability HUD — Multiloader (Fabric 1.16.5 · Forge 1.21)

Мод отображает прочность предметов на HUD прямо во время игры.  
Поддерживает **Fabric 1.16.5** и **Forge 1.21** из одного репозитория.

---

## Требования

| Платформа | Minecraft | Загрузчик | Java |
|-----------|-----------|-----------|------|
| Fabric | **1.16.5** | Fabric Loader ≥ 0.14.0 + [Fabric API](https://modrinth.com/mod/fabric-api) для 1.16.5 | 16+ |
| Forge  | **1.21**   | Forge 51.x | 21+ |

---

## Что показывает мод

| Иконка | Слот |
|--------|------|
| ⚔ | Основная рука |
| 🛡 | Вторая рука |
| ⛑ | Шлем |
| 👕 | Нагрудник |
| 👖 | Поножи |
| 👢 | Ботинки |

Цвета: 🟢 >50% · 🟡 25–50% · 🟠 10–25% · 🔴 <10%

---

## Структура проекта

```
durability-hud/
├── common/          # Платформо-независимый код (DurabilityEntry)
├── fabric/          # Fabric 1.16.5 (HudRenderCallback, Log4j, Tessellator API)
└── forge/           # Forge  1.21  (IGuiOverlay, GuiGraphics API)
```

---

## Сборка

```bash
# Только Fabric
./gradlew :fabric:build

# Только Forge
./gradlew :forge:build

# Обе платформы
./gradlew build
```

Артефакты появятся в:
- `fabric/build/libs/durability-hud-fabric-1.16.5-<version>.jar`
- `forge/build/libs/durability-hud-forge-1.21-<version>.jar`

---

## Ключевые различия API

| | Fabric 1.16.5 | Forge 1.21 |
|---|---|---|
| Рендер HUD | `HudRenderCallback` | `IGuiOverlay` / `RegisterGuiOverlaysEvent` |
| Рисование прямоугольников | `Tessellator` + `RenderSystem` | `GuiGraphics.fill()` |
| Текст | `TextRenderer.drawWithShadow()` | `GuiGraphics.drawString()` |
| Стек матриц | `MatrixStack` (Fabric-yarn) | `PoseStack` (Mojmap) |
| Логирование | `Log4j LogManager` | `LogUtils.getLogger()` (SLF4J) |
| Метаданные | `fabric.mod.json` | `META-INF/mods.toml` |
