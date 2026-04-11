# Durability HUD — Minecraft 1.16.5 (Fabric)

Мод отображает прочность предметов на HUD прямо во время игры.

## Требования
- Minecraft **1.16.5**
- [Fabric Loader](https://fabricmc.net/use/installer/) ≥ 0.14.0
- [Fabric API](https://modrinth.com/mod/fabric-api) для 1.16.5
- **Java 16+** (для сборки)

## Что показывает мод

| Иконка | Слот |
|--------|------|
| ⚔ | Основная рука |
| 🛡 | Вторая рука |
| ⛑ | Шлем |
| 👕 | Нагрудник |
| 👖 | Поножи |
| 👢 | Ботинки |

Цвета: 🟢 >50% · 🟡 25-50% · 🟠 10-25% · 🔴 <10%

## Сборка

```bash
# JDK 16 обязателен (не 17+, не 11)
java -version  # должно быть 16.x

cd durability-hud-1165

# Первый запуск скачает ~150 МБ зависимостей
./gradlew build

# Готовый файл:
# build/libs/durability-hud-1.0.0.jar
```

Скопируй `.jar` в `.minecraft/mods/` и запускай с профилем Fabric.

## Ключевые отличия от версии 1.21.1

| Аспект | 1.16.5 | 1.21.1 |
|--------|--------|--------|
| Callback | `HudRenderCallback(MatrixStack, float)` | `HudRenderCallback(DrawContext, RenderTickCounter)` |
| Рисование фона | `Tessellator` + `RenderSystem` вручную | `DrawContext.fill()` |
| Текст | `TextRenderer.drawWithShadow(MatrixStack, ...)` | `DrawContext.drawText(...)` |
| Размер окна | `client.getWindow().getScaledWidth()` | `context.getScaledWindowWidth()` |
| Логгер | `LogManager` (Log4j) | `LoggerFactory` (SLF4J) |
| Java target | 16 | 21 |
