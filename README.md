# GuestDock 🏠🔑

**Accès invité temporaire pour Home Assistant — sans compte, sans configuration.**

GuestDock est une application Android indépendante qui transforme votre tablette murale en portail d'accès invité pour Home Assistant. En quelques secondes, créez un QR code que vos invités scannent depuis leur téléphone pour contrôler uniquement les appareils que vous avez autorisés.

---

## ✨ Fonctionnalités

- **QR code en un tap** — générez un accès temporaire immédiatement
- **Proxy sécurisé** — l'invité ne touche jamais Home Assistant directement
- **Entités filtrées** — chaque session expose uniquement les appareils autorisés
- **Expiration automatique** — 2 h, 4 h, 8 h, 24 h ou 48 h
- **PWA incluse** — l'invité utilise son navigateur, aucune installation requise
- **Révocation instantanée** — coupez l'accès à tout moment

## 🏗️ Architecture

```
Téléphone invité
  └─ Browser → http://<ip-tablette>:8765/guest/<token>
                       │
              [GuestServer (NanoHTTPD)]
                       │
                 Validation token
                       │
                 Filtre entités
                       │
              [HaClient (OkHttp)]
                       │
              Home Assistant REST API
```

L'invité ne connaît ni l'URL ni le token Home Assistant — tout passe par le proxy GuestServer qui valide et filtre chaque requête.

## 📱 Prérequis

- Android 8.0 (API 26) minimum
- Tablette sur le même réseau Wi-Fi que Home Assistant
- Token longue durée Home Assistant

## 🚀 Installation

1. Téléchargez le dernier APK depuis les [Releases](https://github.com/psycho1807/GuestDock/releases)
2. Installez l'APK (activez "Sources inconnues" si nécessaire)
3. Au premier lancement, entrez l'URL locale de votre HA et votre token

## 🔧 Création d'un token Home Assistant

Dans HA : **Profil → Sécurité → Tokens longue durée → Créer un token**

## 📡 Utilisation

1. Ouvrez GuestDock → appuyez sur **+**
2. Nommez l'invité et choisissez la durée
3. Montrez le QR code à l'invité — il scanne et accède immédiatement
4. Pour révoquer : ouvrez la session → **Révoquer**

## 🛡️ Sécurité

- Le token invité est un code aléatoire 12 caractères (alphanumérique)
- Chaque requête valide le token et vérifie que l'entité cible est dans la liste autorisée
- Aucun token HA n'est transmis au client

## 📄 Licence

GPL v3 — voir [LICENSE](LICENSE)

---

*Projet connexe : [WallDock Pro](https://github.com/psycho1807/WallDock) — application kiosque pour tablette murale Home Assistant*
