# 🎭 AI Persona Roleplay Telegram Bot

🤖 **Telegram Bot:** [@TalkingAndConversationBot](https://t.me/TalkingAndConversationBot)

---

## 🇬🇧 English

### Overview
A Telegram bot built with **Java 21** and **Spring Boot 3** that lets users talk to any historical, philosophical, or fictional persona (e.g., Socrates, Alexander the Great, Albert Einstein, Sherlock Holmes) powered by modern LLMs via **Groq Cloud API**.

### Features
* **Roleplay Any Persona:** Enter any character's name, and the bot will stay in character.
* **Quick Selection & Randomizer:** Inline buttons for popular characters and a random persona generator.
* **Interactive Suggestions:** Ask the AI to suggest discussion topics or generate a historical dossier.
* **Smart Memory Management:** Separate actions for clearing AI context and deleting physical chat messages in Telegram.
* **Auto-Cleanup:** Automatically clears inactive chats and session memory after 30 minutes of inactivity.

### Tech Stack
* **Java 21** & **Spring Boot 3**
* **TelegramBots Spring Boot Starter** (Long Polling)
* **Groq API** (`openai/gpt-oss-120b`, `qwen/qwen3.8-27b`)
* **Docker** (Multi-stage build)

### Environment Variables
| Variable | Description |
| :--- | :--- |
| `TELEGRAM_BOT_TOKEN` | Bot API token from [@BotFather](https://t.me/botfather) |
| `TELEGRAM_BOT_USERNAME` | Bot username without `@` (`TalkingAndConversationBot`) |
| `GROQ_API_KEY` | Free API key from [Groq Console](https://console.groq.com) |
| `GROQ_MODEL` | LLM model name (e.g. `openai/gpt-oss-120b`) |

### How to Run Locally

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Bolot-Azatov/conversation_bot.git
   cd conversation_bot

2.  Set environment variables and run with Maven:

    export TELEGRAM_BOT_TOKEN="your_token"
    export TELEGRAM_BOT_USERNAME="TalkingAndConversationBot"
    export GROQ_API_KEY="your_groq_key"
    export GROQ_MODEL="openai/gpt-oss-120b"

    ./mvnw spring-boot:run

3.  Or run with Docker:

    docker build -t conversation-bot .
    docker run -e TELEGRAM_BOT_TOKEN="your_token" -e GROQ_API_KEY="your_groq_key" conversation-bot

🇷🇺 Русский

Описание

Telegram-бот на Java 21 и Spring Boot 3, позволяющий вести живой диалог с любой
исторической или вымышленной личностью (Сократ, Александр Македонский, Альберт
Эйнштейн, Шерлок Холмс и др.) на базе бесплатных языковых моделей через Groq
API.

Основные возможности

  - Любой собеседник на выбор: напишите имя любой личности, и бот полностью
    войдет в ее роль.
  - Быстрый выбор и рандомайзер: инлайн-кнопки с популярными персонажами и
    кнопка «Случайный герой».
  - Подсказки и Досье: генерация интересных тем для беседы и исторической
    справки о выбранной личности.
  - Управление памятью: раздельные функции сброса контекста ИИ и физического
    удаления сообщений из чата Telegram.
  - Авто-очистка: автоматическое удаление сообщений и очистка памяти для чатов,
    неактивных более 30 минут.

Стек технологий

  - Java 21 / Spring Boot 3
  - TelegramBots Spring Boot Starter (Long Polling)
  - Groq API (openai/gpt-oss-120b, qwen/qwen3.8-27b)
  - Docker (Multi-stage сборка)

Переменные окружения

| Переменная              | Описание                                                    |
| :---------------------- | :---------------------------------------------------------- |
| `TELEGRAM_BOT_TOKEN`    | Токен бота от [@BotFather](https://t.me/botfather)          |
| `TELEGRAM_BOT_USERNAME` | Юзернейм бота без `@` (`TalkingAndConversationBot`)         |
| `GROQ_API_KEY`          | Бесплатный ключ от [Groq Console](https://console.groq.com) |
| `GROQ_MODEL`            | Модель ИИ (например: `openai/gpt-oss-120b`)                 |

Запуск проекта

1.  Клонируйте репозиторий:

    git clone https://github.com/Bolot-Azatov/conversation_bot.git
    cd conversation_bot

2.  Установите переменные окружения и запустите:

    ./mvnw spring-boot:run

3.  Либо запустите через Docker:

    docker build -t conversation-bot .
    docker run -e TELEGRAM_BOT_TOKEN="ваш_токен" -e GROQ_API_KEY="ваш_ключ" conversation-bot

