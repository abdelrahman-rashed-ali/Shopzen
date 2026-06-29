# Shopzen Design System
**Project:** Shopzen — Android E-Commerce
**Style:** Minimal Premium / Modern Soft UI
**Version:** 1.0
**Themes:** Light + Dark (both first-class)
**Purpose:** AI-Optimized Unified Design System

---

# Table of Contents

1. [Design Philosophy](#1-design-philosophy)
2. [Color Palette](#2-color-palette)
3. [Semantic Color Tokens](#3-semantic-color-tokens)
4. [Elevation System](#4-elevation-system)
5. [Typography](#5-typography)
6. [Shadows](#6-shadows)
7. [Borders](#7-borders)
8. [Border Radius](#8-border-radius)
9. [Spacing](#9-spacing)
10. [Buttons](#10-buttons)
11. [Inputs](#11-inputs)
12. [Cards](#12-cards)
13. [Navigation](#13-navigation)
14. [Icons](#14-icons)
15. [Motion](#15-motion)
16. [Accessibility](#16-accessibility)
17. [Design Tokens — Complete Library](#17-design-tokens--complete-library)
18. [AI Implementation Rules](#18-ai-implementation-rules)
19. [Do & Don't Guidelines](#19-do--dont-guidelines)
20. [Migration — Light to Dark](#20-migration--light-to-dark)

---

# 1. Design Philosophy

## Core Personality

**Keywords:** Premium · Minimal · Soft · Spacious · Elegant · Modern · Content-first · Apple-inspired

Primary goals:
- Maximize whitespace / darkspace
- Reduce cognitive load
- Large touch targets
- Soft rounded surfaces
- Monochromatic base — product colors are the only accent
- Visual hierarchy through spacing, not borders
- Almost no decorative elements
- Typography carries hierarchy
- Interface disappears behind products

## Light Mode

White canvas. Products louder than UI. Navigation fades into background. Interface feels light and expensive.

## Dark Mode

Dark theme is **not an inversion** — reinterpretation of same premium minimal language in low-light environment.

Multiple surface tiers replace single white canvas. Products more vibrant against dark surroundings. Experience feels like dimly lit luxury store — calm, focused, expensive.

### Dark Core Principles

- **Luminance creates hierarchy**, not borders or heavy shadows
- **Near-black, never pure black** — except OLED mode
- **Soft ambient glow** replaces traditional drop shadows
- **Warm-tinted darks** — prevents cold, clinical feel of neutral grays
- **Text is warm off-white** — never `#FFFFFF` — reduces eye strain

### Reference DNA (Dark)

Inspired by: Apple Music, App Store Dark, Linear, Arc Browser, Spotify Premium, Vercel Dashboard, Notion Dark.

**Anti-inspiration:** Material Design 2 dark, gray-everything generics, neon cyberpunk.

---

## Design Principles

### 1. White Space / Dark Space First
Everything breathes. Never compress components. Space is a design element.

### 2. Rounded Everywhere
Nothing is visually sharp.

| Element | Radius |
|---|---|
| Buttons | 9999dp |
| Cards | 24dp |
| Search bar | 18dp |
| Images | 18dp |
| Bottom sheet | 32dp |

### 3. Cards Instead of Sections
Every content group lives inside a floating card. Cards create structure instead of separators.

### 4. Soft Depth
Very little elevation. Prefer shadows / opacity / blur / luminance over borders.

### 5. Content Dominates
Products visually louder than UI. Navigation fades into background.

---

# 2. Color Palette

## Light — Base Colors

| Role | HEX |
|---|---|
| Primary Background | `#FFFFFF` |
| Secondary Background | `#F8F8F8` |
| Card Background | `#FFFFFF` |
| Divider | `#EEEEEE` |
| Border | `#E7E7E7` |
| Primary Text | `#111111` |
| Secondary Text | `#6E6E73` |
| Disabled Text | `#B4B4B8` |
| Icon | `#222222` |
| Placeholder | `#B7B7B7` |
| Success | `#34C759` |
| Error | `#FF3B30` |
| Warning | `#FF9500` |
| Primary Button BG | `#000000` |
| Primary Button FG | `#FFFFFF` |
| Secondary Button BG | `#F3F3F3` |
| Secondary Button FG | `#111111` |

---

## Dark — Grayscale Foundation

Warm-tinted near-black base. Each step adds ~6-8% luminance. Warmth prevents sterile feel of neutral grays.

| Name | HEX | HSL | Role |
|---|---|---|---|
| `gray-1000` | `#0A0A0B` | `240 8% 5%` | OLED background |
| `gray-950` | `#111113` | `240 8% 7%` | Primary background |
| `gray-900` | `#161618` | `240 6% 9%` | Secondary background |
| `gray-850` | `#1C1C1F` | `240 5% 12%` | Tertiary background / sections |
| `gray-800` | `#232325` | `240 4% 15%` | Card surface |
| `gray-750` | `#2A2A2D` | `240 4% 18%` | Elevated card / dialog |
| `gray-700` | `#313134` | `240 4% 20%` | Input background |
| `gray-650` | `#38383C` | `240 4% 23%` | Border / hover |
| `gray-600` | `#48484C` | `240 3% 29%` | Disabled icon |
| `gray-500` | `#636366` | `240 2% 39%` | Disabled text / placeholder |
| `gray-400` | `#8E8E93` | `240 2% 56%` | Tertiary text / secondary icon |
| `gray-300` | `#AEAEB2` | `240 2% 69%` | Secondary text |
| `gray-100` | `#EBEBF0` | `240 5% 93%` | Primary text |
| `gray-50` | `#F2F2F7` | `240 5% 95%` | Inverse text |

> **Primary text is `#EBEBF0`, not `#FFFFFF`.** Pure white on very dark backgrounds causes eye fatigue. `#EBEBF0` = ~15:1 contrast — AAA compliant, softer, more premium.

---

# 3. Semantic Color Tokens

Every token: role + light value + dark value + WCAG contrast + usage rules.

---

## Backgrounds

| Token | Light | Dark | Notes |
|---|---|---|---|
| `color.background.primary` | `#FFFFFF` | `#111113` | Main screen canvas |
| `color.background.secondary` | `#F8F8F8` | `#161618` | Section containers |
| `color.background.tertiary` | `#F3F3F3` | `#1C1C1F` | Nested sections, filter drawers |
| `color.background.oled` | — | `#0A0A0B` | OLED mode only |

---

## Surfaces

| Token | Light | Dark | Notes |
|---|---|---|---|
| `color.surface.base` | `#FFFFFF` | `#1C1C1F` | Base surface above background |
| `color.surface.card` | `#FFFFFF` | `#232325` | Standard cards |
| `color.surface.elevated` | `#FFFFFF` | `#2A2A2D` | Pressed/hover cards |
| `color.surface.dialog` | `#FFFFFF` | `#2A2A2D` | Dialogs, bottom sheets |
| `color.surface.tooltip` | `#FFFFFF` | `#38383C` | Highest elevation |
| `color.surface.input` | `#F8F8F8` | `#313134` | Text field / search bg |
| `color.surface.navigation` | `#FFFFFF` | `#161618` | Top app bar |
| `color.surface.bottomNavigation` | `#FFFFFF` | `#161618` | Bottom nav bar |
| `color.image.well` | `#F8F8F8` | `#1C1C1F` | Behind product images in cards |
| `color.image.placeholder` | `#F3F3F3` | `#232325` | Loading placeholder |

---

## Overlays

| Token | Light | Dark |
|---|---|---|
| `color.overlay` | `rgba(0,0,0,0.40)` | `rgba(0,0,0,0.60)` |
| `color.overlay.light` | `rgba(0,0,0,0.20)` | `rgba(0,0,0,0.30)` |

---

## Text

| Token | Light | Dark | WCAG Light | WCAG Dark |
|---|---|---|---|---|
| `color.text.primary` | `#111111` | `#EBEBF0` | 19.4:1 AAA | 15.2:1 AAA |
| `color.text.secondary` | `#6E6E73` | `#AEAEB2` | 5.9:1 AA | 7.4:1 AA |
| `color.text.tertiary` | `#8E8E93` | `#8E8E93` | 4.5:1 AA | 4.6:1 AA |
| `color.text.disabled` | `#B4B4B8` | `#636366` | ~3:1 | ~3:1 |
| `color.text.placeholder` | `#B7B7B7` | `#636366` | muted | muted |
| `color.text.hint` | `#B7B7B7` | `#8E8E93` | muted | muted |
| `color.text.inverse` | `#FFFFFF` | `#111113` | on dark btn | on white btn |
| `color.text.error` | `#FF3B30` | `#FF6B6B` | — | 5.8:1 AA |
| `color.text.success` | `#34C759` | `#34D058` | — | 8.4:1 AA |
| `color.text.warning` | `#FF9500` | `#FFB347` | — | 7.1:1 AA |
| `color.text.link` | `#007AFF` | `#6B9FFF` | — | 5.4:1 AA |

> **Dark:** hierarchy uses luminance, not weight. Do not bold secondary text for emphasis.

---

## Icons

| Token | Light | Dark |
|---|---|---|
| `color.icon.primary` | `#222222` | `#EBEBF0` |
| `color.icon.secondary` | `#6E6E73` | `#8E8E93` |
| `color.icon.inactive` | `#B4B4B8` | `#636366` |
| `color.icon.disabled` | `#D1D1D6` | `#48484C` |
| `color.icon.destructive` | `#FF3B30` | `#FF6B6B` |
| `color.icon.notification` | `#FF3B30` | `#FF6B6B` |
| `color.icon.rating` | `#FF9500` | `#FFB347` |
| `color.icon.wishlist.empty` | `#B4B4B8` | `#AEAEB2` |
| `color.icon.wishlist.filled` | `#FF3B30` | `#FF6B6B` |

---

## Actions (Buttons)

| Token | Light | Dark |
|---|---|---|
| `color.action.primary.background` | `#000000` | `#FFFFFF` |
| `color.action.primary.foreground` | `#FFFFFF` | `#111113` |
| `color.action.secondary.background` | `#F3F3F3` | `#2A2A2D` |
| `color.action.secondary.foreground` | `#111111` | `#EBEBF0` |
| `color.action.secondary.border` | none | `#48484C` |
| `color.action.disabled.background` | `#F3F3F3` | `#232325` |
| `color.action.disabled.foreground` | `#B4B4B8` | `#636366` |
| `color.action.danger.background` | `#FFF0F0` | `rgba(255,107,107,0.12)` |
| `color.action.danger.foreground` | `#FF3B30` | `#FF6B6B` |
| `color.action.success.background` | `#F0FFF4` | `rgba(52,208,88,0.12)` |
| `color.action.success.foreground` | `#34C759` | `#34D058` |

> **Dark primary button = white.** Single bright beacon on dark canvas. One per screen — mandatory.

---

## Status

| Token | Light | Dark |
|---|---|---|
| `color.success` | `#34C759` | `#34D058` |
| `color.warning` | `#FF9500` | `#FFB347` |
| `color.error` | `#FF3B30` | `#FF6B6B` |
| `color.info` | `#007AFF` | `#6B9FFF` |

---

## Borders

| Token | Light | Dark |
|---|---|---|
| `color.border.default` | `#E7E7E7` | `#38383C` |
| `color.border.subtle` | `#EEEEEE` | `#2A2A2D` |
| `color.border.focus` | `#000000` | `#EBEBF0` |
| `color.border.error` | `#FF3B30` | `#FF6B6B` |
| `color.border.success` | `#34C759` | `#34D058` |
| `color.border.selected` | `#000000` | `#EBEBF0` |
| `color.border.nav` | none | `rgba(235,235,240,0.08)` |

---

## Dividers

| Token | Light | Dark |
|---|---|---|
| `color.divider` | `#EEEEEE` | `#2A2A2D` |
| `color.divider.strong` | `#E7E7E7` | `#313134` |

---

## Interaction States

| Token | Light | Dark |
|---|---|---|
| `color.selection` | `rgba(0,0,0,0.08)` | `rgba(235,235,240,0.15)` |
| `color.focus` | `rgba(0,0,0,0.12)` | `rgba(235,235,240,0.20)` |
| `color.ripple` | `rgba(0,0,0,0.06)` | `rgba(235,235,240,0.10)` |
| `color.hover` | `rgba(0,0,0,0.04)` | `rgba(235,235,240,0.06)` |
| `color.pressed` | `rgba(0,0,0,0.08)` | `rgba(235,235,240,0.12)` |

---

## Skeleton / Loading

| Token | Light | Dark |
|---|---|---|
| `color.skeleton.base` | `#F3F3F3` | `#232325` |
| `color.skeleton.highlight` | `#EBEBEB` | `#2A2A2D` |

---

# 4. Elevation System

## Light Elevation

Very subtle. Prefer shadows + opacity + blur over borders.

| Level | Shadow |
|---|---|
| Card | Y:4 Blur:18 Opacity:8% black |
| Floating Button | Y:8 Blur:24 Opacity:12% black |

No heavy shadows.

---

## Dark Elevation

Traditional shadows nearly invisible on dark. Elevation via **luminance steps + ambient glow**.

| Level | Name | Surface | Shadow |
|---|---|---|---|
| 0 | Ground | `#111113` | none |
| 1 | Raised | `#1C1C1F` | none |
| 2 | Card | `#232325` | Y:4 Blur:12 rgba(0,0,0,0.20) |
| 3 | Elevated Card | `#2A2A2D` | Y:6 Blur:20 rgba(0,0,0,0.30) |
| 4 | Dialog | `#2A2A2D` | Y:16 Blur:40 rgba(0,0,0,0.50) |
| 5 | Floating | `#38383C` | Y:20 Blur:48 rgba(0,0,0,0.60) |

**Ambient glow (elevation 5):**
```
inset 0 1px 0 rgba(255,255,255,0.06)
```
Simulates light catching top edge of elevated surface. Never use colored glows.

---

# 5. Typography

## Font

Modern geometric sans — use in order of preference:
1. **Plus Jakarta Sans** (preferred)
2. Inter
3. SF Pro

---

## Type Scale (Theme-Independent)

| Style | Size | Weight |
|---|---|---|
| Display | 32 | Bold 700 |
| Heading 1 | 28 | Bold 700 |
| Heading 2 | 24 | Bold 700 |
| Heading 3 | 20 | Semibold 600 |
| Product Title | 18 | Semibold 600 |
| Section Title | 16 | Semibold 600 |
| Body | 15 | Regular 400 |
| Small | 13 | Regular 400 |
| Caption | 12 | Regular 400 |
| Price | 20 | Bold 700 |
| Badge | 11 | Medium 500 |

```
Regular  400
Medium   500
SemiBold 600
Bold     700
```

---

## Typography Color Roles

| Role | Light | Dark | WCAG Dark |
|---|---|---|---|
| Primary | `#111111` | `#EBEBF0` | 15.2:1 AAA |
| Secondary | `#6E6E73` | `#AEAEB2` | 7.4:1 AA |
| Tertiary | `#8E8E93` | `#8E8E93` | 4.6:1 AA |
| Disabled | `#B4B4B8` | `#636366` | ~3:1 |
| Inverse | `#FFFFFF` | `#111113` | AAA |
| Caption | `#6E6E73` | `#8E8E93` | 4.6:1 AA |
| Hint | `#B7B7B7` | `#636366` | muted |
| Error | `#FF3B30` | `#FF6B6B` | 5.8:1 AA |
| Success | `#34C759` | `#34D058` | 8.4:1 AA |
| Warning | `#FF9500` | `#FFB347` | 7.1:1 AA |
| Link | `#007AFF` | `#6B9FFF` | 5.4:1 AA |
| Price | `#111111` | `#EBEBF0` | AAA |
| Badge | `#111111` | `#EBEBF0` | AAA |

---

## Typography Rules

- Geometric sans-serif only
- Headings: bold or semibold
- Body: regular weight
- Prices: always Bold, stronger than descriptions
- Never mix more than 3 font sizes within single component
- **Dark:** emphasis via luminance, not font weight increase

---

# 6. Shadows

## Light Shadows

| Level | Y | Blur | Opacity | Use |
|---|---|---|---|---|
| Card | 4 | 18 | 8% black | Standard cards |
| FAB | 8 | 24 | 12% black | Floating button |

No heavy shadows.

---

## Dark Shadows

| Level | Y | Blur | Color | Use |
|---|---|---|---|---|
| none | — | — | — | Elevation 0 |
| xs | 2 | 8 | rgba(0,0,0,0.20) | Subtle |
| sm | 4 | 12 | rgba(0,0,0,0.25) | Standard cards |
| md | 8 | 20 | rgba(0,0,0,0.35) | Elevated panels |
| lg | 16 | 40 | rgba(0,0,0,0.50) | Dialogs |
| xl | 20 | 48 | rgba(0,0,0,0.60) | FAB, modals |

---

# 7. Borders

## Rules (Both Themes)

- Borders rare — only when luminance contrast insufficient
- Never decorative structure
- Never on cards

## Usage Table

| Use Case | Light Border | Dark Border | Thickness |
|---|---|---|---|
| Input (default) | `#E7E7E7` | `#38383C` | 1px |
| Input (focused) | `#000000` | `#EBEBF0` | 1.5px |
| Input (error) | `#FF3B30` | `#FF6B6B` | 1.5px |
| Quantity button | `#E7E7E7` | `#38383C` | 1px |
| Swatch (selected) | `#000000` | `#EBEBF0` | 2px |
| Cards | none | none | — |
| Navigation | none | none | — |
| Bottom nav top | none | `rgba(235,235,240,0.08)` | 0.5px |

---

# 8. Border Radius

Identical in both themes. Shape language never changes.

| Token | Value | Usage |
|---|---|---|
| `radius-xs` | 8dp | Small badges, tags |
| `radius-sm` | 12dp | Chips, tags |
| `radius-md` | 16dp | Inputs, smaller cards |
| `radius-lg` | 20dp | Standard cards |
| `radius-xl` | 24dp | Product cards, major cards |
| `radius-2xl` | 28dp | Bottom sheets |
| `radius-full` | 9999dp | Buttons, pills, circles |

---

# 9. Spacing

8pt grid. Identical in both themes.

| Token | px |
|---|---|
| `space.xs` | 4 |
| `space.sm` | 8 |
| `space.md` | 12 |
| `space.lg` | 16 |
| `space.xl` | 20 |
| `space.2xl` | 24 |
| `space.3xl` | 32 |
| `space.4xl` | 40 |
| `space.5xl` | 48 |

Layout constants: screen H-padding **20dp** · section V-rhythm **24-32dp** · card padding **16dp**

---

# 10. Buttons

## Primary Button

Light: black fill, white text. Dark: white fill, dark text — single beacon on dark canvas. One per screen.

```
Height:    52dp
Radius:    radius-full (9999dp)
Animation: Scale 96% on press, 200ms ease-out
```

| State | Light BG | Light FG | Dark BG | Dark FG |
|---|---|---|---|---|
| Default | `#000000` | `#FFFFFF` | `#FFFFFF` | `#111113` |
| Pressed | `#333333` | `#FFFFFF` | `#E5E5EA` | `#111113` |
| Focused | `#000000` + ring | `#FFFFFF` | `#FFFFFF` + ring | `#111113` |
| Hovered | `#1A1A1A` | `#FFFFFF` | `#F2F2F7` | `#111113` |
| Disabled | `#F3F3F3` | `#B4B4B8` | `#232325` | `#636366` |
| Loading | BG + spinner | — | BG + spinner | — |

Shadow light: Y:4 Blur:8 rgba(0,0,0,0.20)
Shadow dark: Y:8 Blur:24 rgba(255,255,255,0.12)

---

## Secondary Button

```
Height: 52dp  |  Radius: radius-full  |  Scale 96% on press
```

| State | Light BG | Light FG | Dark BG | Dark FG | Dark Border |
|---|---|---|---|---|---|
| Default | `#F3F3F3` | `#111111` | `#2A2A2D` | `#EBEBF0` | `#48484C` |
| Pressed | `#E8E8E8` | `#111111` | `#313134` | `#EBEBF0` | `#636366` |
| Focused | `#F3F3F3` | `#111111` | `#2A2A2D` | `#EBEBF0` | `#EBEBF0` 1.5px |
| Disabled | `#F9F9F9` | `#B4B4B8` | `#1C1C1F` | `#636366` | `#232325` |

---

## Outlined Button

```
Background: transparent  |  Height: 52dp  |  Radius: radius-full
```

| State | Light Border/FG | Dark Border/FG |
|---|---|---|
| Default | `#000000` / `#111111` | `#EBEBF0` / `#EBEBF0` |
| Pressed | BG rgba(0,0,0,0.06) | BG rgba(235,235,240,0.08) |
| Focused | ring 2px | ring 2px |
| Disabled | `#D1D1D6` / `#B4B4B8` | `#48484C` / `#636366` |

---

## Ghost Button

```
Background: transparent  |  Border: none
```

| State | Light BG | Dark BG |
|---|---|---|
| Default | transparent | transparent |
| Pressed | rgba(0,0,0,0.06) | rgba(235,235,240,0.08) |
| Focused | rgba(0,0,0,0.08) | rgba(235,235,240,0.12) |
| Disabled | transparent | transparent / `#636366` |

---

## Danger Button

```
Height: 52dp  |  Radius: radius-full
```

| | Light | Dark |
|---|---|---|
| BG | `#FFF0F0` | `rgba(255,107,107,0.12)` |
| FG | `#FF3B30` | `#FF6B6B` |
| Border | `rgba(255,59,48,0.25)` | `rgba(255,107,107,0.25)` |
| Pressed BG | `#FFD9D7` | `rgba(255,107,107,0.20)` |

---

## Success Button

```
Height: 52dp  |  Radius: radius-full
```

| | Light | Dark |
|---|---|---|
| BG | `#F0FFF4` | `rgba(52,208,88,0.12)` |
| FG | `#34C759` | `#34D058` |
| Border | `rgba(52,199,89,0.25)` | `rgba(52,208,88,0.25)` |

---

## Disabled Button (Universal)

```
Light:   BG #F3F3F3 / FG #B4B4B8
Dark:    BG #232325 / FG #636366
Opacity: 1.0 — never use alpha reduction
```

> Never use `alpha(0.38)` for disabled. Explicit disabled colors only.

---

## Quantity Button

```
Size:    40x40dp  |  Radius: radius-full
Light:   BG #F3F3F3 / icon #222222
Dark:    BG #232325 / icon #EBEBF0 / border 1px #38383C
Pressed: Light #E8E8E8 / Dark #2A2A2D
```

---

# 11. Inputs

## Search Bar

```
Height: 48dp  |  Radius: 18dp
```

| | Light | Dark |
|---|---|---|
| BG | `#F8F8F8` | `#313134` |
| Text | `#111111` | `#EBEBF0` |
| Placeholder | `#B7B7B7` | `#636366` |
| Icon | `#6E6E73` | `#8E8E93` |
| Focused border | `#000000` 1.5px | `#EBEBF0` 1.5px |
| Disabled BG | `#EEEEEE` | `#1C1C1F` |

---

## Text Field

```
Height: 52dp  |  Radius: radius-md (16dp)
```

| State | Light BG | Light Border | Dark BG | Dark Border |
|---|---|---|---|---|
| Default | `#F8F8F8` | `#E7E7E7` | `#313134` | `#38383C` |
| Focused | `#F8F8F8` | `#000000` 1.5px | `#313134` | `#EBEBF0` 1.5px |
| Error | `#FFF0F0` | `#FF3B30` 1.5px | `#313134` | `#FF6B6B` 1.5px |
| Disabled | `#F3F3F3` | `#EEEEEE` | `#1C1C1F` | `#232325` |
| Read Only | `#F8F8F8` | `#EEEEEE` | `#1C1C1F` | `#232325` |

Text: light `#111111` / dark `#EBEBF0`
Label: light `#6E6E73` focused `#111111` / dark `#8E8E93` focused `#EBEBF0`
Error label: light `#FF3B30` / dark `#FF6B6B`

---

## Coupon Field

| | Light | Dark |
|---|---|---|
| BG | `#F8F8F8` | `#313134` |
| Applied border | `#34C759` | `#34D058` |
| Invalid border | `#FF3B30` | `#FF6B6B` |

---

## Dropdown

| | Light | Dark |
|---|---|---|
| BG | `#F8F8F8` | `#313134` |
| Border | `#E7E7E7` | `#38383C` |
| Open border | `#000000` | `#EBEBF0` |
| Panel BG | `#FFFFFF` | `#2A2A2D` |

---

## Checkbox

| State | Light | Dark |
|---|---|---|
| Unchecked | border `#D1D1D6` 1.5px | border `#48484C` 1.5px |
| Checked | BG `#000000`, check `#FFFFFF` | BG `#FFFFFF`, check `#111113` |
| Focused | border `#000000` | border `#EBEBF0` |
| Disabled | BG `#F3F3F3`, border `#E7E7E7` | BG `#232325`, border `#38383C` |

---

## Radio

| State | Light | Dark |
|---|---|---|
| Unchecked | border `#D1D1D6` 1.5px | border `#48484C` 1.5px |
| Selected | ring `#000000`, fill `#000000` | ring `#EBEBF0`, fill `#EBEBF0` |
| Disabled | ring `#D1D1D6` | ring `#38383C` |

---

## Switch / Toggle

| State | Light Track | Light Thumb | Dark Track | Dark Thumb |
|---|---|---|---|---|
| Off | `#E5E5EA` | `#FFFFFF` | `#313134` | `#636366` |
| On | `#000000` | `#FFFFFF` | `#FFFFFF` | `#111113` |
| Disabled Off | `#F3F3F3` | `#D1D1D6` | `#232325` | `#48484C` |
| Disabled On | `#8E8E93` | `#FFFFFF` | `#48484C` | `#232325` |

---

## Slider

| | Light | Dark |
|---|---|---|
| Track (unselected) | `#E5E5EA` | `#313134` |
| Track (filled) | `#000000` | `#FFFFFF` |
| Thumb | `#FFFFFF` + shadow | `#FFFFFF` + shadow |

---

## Stepper / Quantity

| | Light | Dark |
|---|---|---|
| BG | `#F3F3F3` | `#232325` |
| Icons | `#111111` | `#EBEBF0` |
| Border | none | `1px #38383C` |

---

## Chips / Tags

| State | Light BG | Light FG | Dark BG | Dark FG |
|---|---|---|---|---|
| Default | `#F3F3F3` | `#111111` | `#232325` | `#AEAEB2` |
| Selected | `#000000` | `#FFFFFF` | `#FFFFFF` | `#111113` |

---

# 12. Cards

## Shared Rules

- No border on cards (both themes)
- Padding: 16dp
- Radius: 24dp
- Pressed: scale 98% + shadow change

---

## Product Card

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#232325` |
| Image well | `#F8F8F8` | `#1C1C1F` |
| Shadow | Y:4 Blur:18 rgba(0,0,0,0.08) | Y:4 Blur:12 rgba(0,0,0,0.20) |
| Pressed BG | `#FAFAFA` | `#2A2A2D` |

Image area: ~60% card height. Well darker than card — product pops.

Wishlist button (floating, top-right, 32x32dp, radius-full):

| | Light | Dark |
|---|---|---|
| BG | white + shadow | rgba(35,35,37,0.80) + blur 8dp |
| Icon empty | `#6E6E73` | `#EBEBF0` |
| Icon filled | `#FF3B30` | `#FF6B6B` |

---

## Category Chip

```
Image circle: 56dp diameter
```

| | Light | Dark |
|---|---|---|
| Circle BG | `#F8F8F8` | `#1C1C1F` |
| Border selected | `2px #000000` | `2px #EBEBF0` |
| Label selected | `#111111` | `#EBEBF0` |
| Label default | `#6E6E73` | `#AEAEB2` |

---

## Cart Item Card

```
Radius: 24dp  |  Padding: 16dp  |  Layout: Horizontal
Delete icon top-right
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#232325` |
| Image well | `#F8F8F8` | `#1C1C1F` |
| Delete icon | `#FF3B30` | `#FF6B6B` |

---

## Profile Card

```
Radius: 24dp  |  Padding: 20dp
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#232325` |
| Avatar border | `2px #E7E7E7` | `2px #38383C` |

---

## Coupon Card

```
Radius: 20dp  |  Dashed border left accent  |  Code: monospace
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#232325` |
| Dashed border | `rgba(0,0,0,0.15)` | `rgba(235,235,240,0.20)` |

---

## Dialog Card

```
Radius: 28dp  |  Padding: 24dp
Scrim: Light rgba(0,0,0,0.40) / Dark rgba(0,0,0,0.60)
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#2A2A2D` |
| Shadow | Y:8 Blur:24 rgba(0,0,0,0.12) | Y:16 Blur:40 rgba(0,0,0,0.50) |
| Title | `#111111` | `#EBEBF0` |
| Message | `#6E6E73` | `#AEAEB2` |

---

## Bottom Sheet

```
Radius: 32dp top only  |  Handle: 4x32dp pill, centered, 12dp from top
Scrim: Light rgba(0,0,0,0.40) / Dark rgba(0,0,0,0.60)
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#232325` |
| Handle | `#D1D1D6` | `#48484C` |
| Shadow | Y:-4 Blur:16 rgba(0,0,0,0.10) | Y:-8 Blur:32 rgba(0,0,0,0.50) |

---

# 13. Navigation

## Screen Hierarchy

**Home:** Header > Search > Hero Banner > Categories > Featured Products > Bottom Nav

**Product Detail:** Image Carousel > Title > Price > Rating > Seller > Color > Tabs > Description > Quantity > Add to Cart

**Cart:** Header > Cart List > Coupon > Summary > Checkout

---

## Top App Bar

```
Height: 56dp + safe area
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#161618` |
| Title | `#111111` 16sp Semibold | `#EBEBF0` 16sp Semibold |
| Icons | `#222222` | `#EBEBF0` |
| Scrolled BG | `#FFFFFF` + shadow | `#1C1C1F` + shadow |

---

## Bottom Navigation

```
Height: 72dp + safe area  |  Icons: 24dp
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#161618` |
| Top border | none | `0.5px rgba(235,235,240,0.08)` |

| State | Light Icon/Label | Dark Icon/Label |
|---|---|---|
| Selected | `#000000` | `#EBEBF0` |
| Inactive | `rgba(0,0,0,0.30)` | `#636366` |
| Pressed | `rgba(0,0,0,0.60)` | `#AEAEB2` |

### Tabs

| # | Label | Route | Auth |
|---|---|---|---|
| 0 | Home | `main/home` | No |
| 1 | Search | `main/search` | No |
| 2 | Wishlist | `main/wishlist` | Yes |
| 3 | Cart | `main/cart` | Yes |
| 4 | Profile | `main/profile` | Yes |

Notification badge: BG light `#FF3B30` / dark `#FF6B6B` · FG `#FFFFFF` · 16dp radius-full

---

## Floating Action Button

```
Diameter: 60dp  |  Radius: radius-full
```

| | Light | Dark |
|---|---|---|
| BG | `#000000` | `#FFFFFF` |
| Icon | `#FFFFFF` | `#111113` |
| Shadow | Y:8 Blur:24 rgba(0,0,0,0.20) | Y:8 Blur:24 rgba(0,0,0,0.40) + inner glow |
| Pressed | `#333333` / scale 96% | `#E5E5EA` / scale 96% |

Dark inner glow: `inset 0 1px 0 rgba(255,255,255,0.20)`

---

## Tab Bar (Inline)

```
Container: radius-full, 4dp padding
```

| State | Light | Dark |
|---|---|---|
| Selected | BG `#000000`, text `#FFFFFF` | BG `#FFFFFF`, text `#111113` |
| Unselected | transparent, text `#6E6E73` | transparent, text `#8E8E93` |
| Container | `#F3F3F3` | `#1C1C1F` |

---

## Navigation Drawer

```
Width: 280dp
```

| | Light | Dark |
|---|---|---|
| BG | `#FFFFFF` | `#161618` |
| Header BG | `#F8F8F8` | `#1C1C1F` |
| Item selected | `rgba(0,0,0,0.08)` bg | `rgba(235,235,240,0.10)` bg |
| Item inactive text | `#6E6E73` | `#AEAEB2` |
| Divider | `#EEEEEE` | `#2A2A2D` |

---

## Carousel Indicators

| | Light | Dark |
|---|---|---|
| Active | `#000000` | `#EBEBF0` |
| Inactive | `#D1D1D6` | `#48484C` |

---

## Color Selector (Swatches)

```
Size: 36x36dp  |  Spacing: 12dp
Selected ring: Light 2px #000000 / Dark 2px #EBEBF0
```

---

## Rating Badge

```
Radius: radius-sm (pill)
Light: BG #111111, text #FFFFFF
Dark:  BG #2A2A2D, text #EBEBF0
Star: Light #FF9500 / Dark #FFB347
```

---

# 14. Icons

## Specifications (Theme-Independent)

- Style: stroke only, rounded caps
- Weight: 2px
- Common: 22dp · Navigation: 24dp · Action: 20dp

## Icon Color States

| State | Light | Dark |
|---|---|---|
| Primary | `#222222` | `#EBEBF0` |
| Secondary | `#6E6E73` | `#8E8E93` |
| Inactive | `#B4B4B8` | `#636366` |
| Selected | `#111111` | `#EBEBF0` |
| Disabled | `#D1D1D6` | `#48484C` |
| Interactive | Primary + ripple | Primary + ripple |
| Notification | `#FF3B30` | `#FF6B6B` |
| Destructive | `#FF3B30` | `#FF6B6B` |

> Icons never compete with product imagery.

## Wishlist Heart

```
Empty:     Stroke 2px — light #6E6E73 / dark #AEAEB2
Filled:    Fill — light #FF3B30 / dark #FF6B6B, no stroke
Animation: Scale 110% to 100%, 250ms spring, on fill
```

## Star Rating

```
Filled: light #FF9500 / dark #FFB347
Empty:  light #D1D1D6 / dark #38383C
Size:   16dp
```

---

# 15. Motion

## Timing (Theme-Independent)

```
Fast:    150ms
Normal:  200ms
Slow:    300ms
Easing:  ease-out / cubic-bezier(0.0, 0.0, 0.2, 1.0)
```

## Animation Catalog

| Interaction | Animation | Duration |
|---|---|---|
| Button press | Scale 96%, shadow reduce | 200ms |
| Card press | Scale 98%, shadow change | 200ms |
| Wishlist fill | Scale 110% to 100% + color fill | 250ms spring |
| Add to Cart | Morph > spinner > checkmark | 300ms |
| Screen enter | Fade + slide Y:16 to 0 | 250ms |
| Modal open | Slide up from bottom | 300ms ease-out |
| Modal dismiss | Slide down + fade | 250ms |
| Skeleton shimmer | base > highlight > base | 1200ms loop |
| Tab switch | Crossfade + indicator slide | 200ms |

## Dark Mode Motion Adjustments

- Reduce glow animations — no pulsing or breathing glows
- No neon animations
- Maintain premium subtle interactions

## Interaction States (All Components)

Every component: Default · Pressed · Focused · Disabled · Loading · Success · Error

## Reduced Motion

When `prefers-reduced-motion` active:
- Remove scale animations
- Replace slides with crossfades (100ms max)
- Remove skeleton shimmer — static placeholder

---

# 16. Accessibility

## Touch Targets (Theme-Independent)

```
Minimum:   44x44dp
Preferred: 48x48dp
```

## Contrast Requirements

| Role | Light | Dark | WCAG |
|---|---|---|---|
| Primary text | 19.4:1 | 15.2:1 | AAA both |
| Secondary text | 5.9:1 | 7.4:1 | AA both |
| Error text | — | 5.8:1 | AA |
| Success text | — | 8.4:1 | AA |
| Primary btn label | 19.4:1 | 19.4:1 | AAA both |
| Disabled | ~3:1 | ~3:1 | Intentional |

## High Contrast Mode

- Primary text: max contrast
- All border widths +1px
- Focus ring: 3px max contrast color
- Disabled: strikethrough or lock icon — not color alone

## Color Blindness

All status states use icon + color, never color alone:
- Error: color + X icon
- Success: color + check icon
- Warning: color + warning icon

## OLED Optimization (Dark Only)

OLED mode: background `#0A0A0B`. Reduce ambient glow intensities 50%.

## Focus Visibility

```
Light: ring #000000, 2px, offset 2dp
Dark:  ring #EBEBF0, 2px, offset 2dp
Radius: component radius + 2dp
```

---

# 17. Design Tokens — Complete Library

```yaml
# ═══════════════════════════════════════════════════
# SHOPZEN DESIGN SYSTEM — COMPLETE TOKEN LIBRARY v1.0
# ═══════════════════════════════════════════════════

# ── BACKGROUNDS ──────────────────────────────────────
color.background.primary:
  light: '#FFFFFF'
  dark:  '#111113'
color.background.secondary:
  light: '#F8F8F8'
  dark:  '#161618'
color.background.tertiary:
  light: '#F3F3F3'
  dark:  '#1C1C1F'
color.background.oled:
  dark:  '#0A0A0B'

# ── SURFACES ─────────────────────────────────────────
color.surface.base:
  light: '#FFFFFF'
  dark:  '#1C1C1F'
color.surface.card:
  light: '#FFFFFF'
  dark:  '#232325'
color.surface.elevated:
  light: '#FFFFFF'
  dark:  '#2A2A2D'
color.surface.dialog:
  light: '#FFFFFF'
  dark:  '#2A2A2D'
color.surface.tooltip:
  light: '#FFFFFF'
  dark:  '#38383C'
color.surface.input:
  light: '#F8F8F8'
  dark:  '#313134'
color.surface.navigation:
  light: '#FFFFFF'
  dark:  '#161618'
color.surface.bottomNavigation:
  light: '#FFFFFF'
  dark:  '#161618'
color.surface.tint:
  light: 'rgba(0,0,0,0.02)'
  dark:  'rgba(235,235,240,0.04)'
color.image.well:
  light: '#F8F8F8'
  dark:  '#1C1C1F'
color.image.placeholder:
  light: '#F3F3F3'
  dark:  '#232325'

# ── OVERLAYS ─────────────────────────────────────────
color.overlay:
  light: 'rgba(0,0,0,0.40)'
  dark:  'rgba(0,0,0,0.60)'
color.overlay.light:
  light: 'rgba(0,0,0,0.20)'
  dark:  'rgba(0,0,0,0.30)'

# ── TEXT ─────────────────────────────────────────────
color.text.primary:
  light: '#111111'
  dark:  '#EBEBF0'
color.text.secondary:
  light: '#6E6E73'
  dark:  '#AEAEB2'
color.text.tertiary:
  light: '#8E8E93'
  dark:  '#8E8E93'
color.text.disabled:
  light: '#B4B4B8'
  dark:  '#636366'
color.text.placeholder:
  light: '#B7B7B7'
  dark:  '#636366'
color.text.hint:
  light: '#B7B7B7'
  dark:  '#8E8E93'
color.text.inverse:
  light: '#FFFFFF'
  dark:  '#111113'
color.text.error:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.text.success:
  light: '#34C759'
  dark:  '#34D058'
color.text.warning:
  light: '#FF9500'
  dark:  '#FFB347'
color.text.link:
  light: '#007AFF'
  dark:  '#6B9FFF'

# ── ICONS ────────────────────────────────────────────
color.icon.primary:
  light: '#222222'
  dark:  '#EBEBF0'
color.icon.secondary:
  light: '#6E6E73'
  dark:  '#8E8E93'
color.icon.inactive:
  light: '#B4B4B8'
  dark:  '#636366'
color.icon.disabled:
  light: '#D1D1D6'
  dark:  '#48484C'
color.icon.destructive:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.icon.notification:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.icon.rating:
  light: '#FF9500'
  dark:  '#FFB347'
color.icon.wishlist.empty:
  light: '#B4B4B8'
  dark:  '#AEAEB2'
color.icon.wishlist.filled:
  light: '#FF3B30'
  dark:  '#FF6B6B'

# ── ACTIONS ──────────────────────────────────────────
color.action.primary.background:
  light: '#000000'
  dark:  '#FFFFFF'
color.action.primary.foreground:
  light: '#FFFFFF'
  dark:  '#111113'
color.action.secondary.background:
  light: '#F3F3F3'
  dark:  '#2A2A2D'
color.action.secondary.foreground:
  light: '#111111'
  dark:  '#EBEBF0'
color.action.secondary.border:
  light: none
  dark:  '#48484C'
color.action.outlined.border:
  light: '#000000'
  dark:  '#EBEBF0'
color.action.outlined.foreground:
  light: '#111111'
  dark:  '#EBEBF0'
color.action.ghost.foreground:
  light: '#111111'
  dark:  '#EBEBF0'
color.action.disabled.background:
  light: '#F3F3F3'
  dark:  '#232325'
color.action.disabled.foreground:
  light: '#B4B4B8'
  dark:  '#636366'
color.action.danger.background:
  light: '#FFF0F0'
  dark:  'rgba(255,107,107,0.12)'
color.action.danger.foreground:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.action.danger.border:
  light: 'rgba(255,59,48,0.25)'
  dark:  'rgba(255,107,107,0.25)'
color.action.success.background:
  light: '#F0FFF4'
  dark:  'rgba(52,208,88,0.12)'
color.action.success.foreground:
  light: '#34C759'
  dark:  '#34D058'
color.action.success.border:
  light: 'rgba(52,199,89,0.25)'
  dark:  'rgba(52,208,88,0.25)'

# ── STATUS ───────────────────────────────────────────
color.success:
  light: '#34C759'
  dark:  '#34D058'
color.warning:
  light: '#FF9500'
  dark:  '#FFB347'
color.error:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.info:
  light: '#007AFF'
  dark:  '#6B9FFF'

# ── BORDERS ──────────────────────────────────────────
color.border.default:
  light: '#E7E7E7'
  dark:  '#38383C'
color.border.subtle:
  light: '#EEEEEE'
  dark:  '#2A2A2D'
color.border.focus:
  light: '#000000'
  dark:  '#EBEBF0'
color.border.error:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.border.success:
  light: '#34C759'
  dark:  '#34D058'
color.border.selected:
  light: '#000000'
  dark:  '#EBEBF0'
color.border.nav:
  light: none
  dark:  'rgba(235,235,240,0.08)'

# ── DIVIDERS ─────────────────────────────────────────
color.divider:
  light: '#EEEEEE'
  dark:  '#2A2A2D'
color.divider.strong:
  light: '#E7E7E7'
  dark:  '#313134'

# ── SHADOWS ──────────────────────────────────────────
color.shadow:
  light: 'rgba(0,0,0,0.10)'
  dark:  'rgba(0,0,0,0.40)'
color.shadow.xs:
  light: 'rgba(0,0,0,0.06)'
  dark:  'rgba(0,0,0,0.20)'
color.shadow.sm:
  light: 'rgba(0,0,0,0.08)'
  dark:  'rgba(0,0,0,0.25)'
color.shadow.md:
  light: 'rgba(0,0,0,0.10)'
  dark:  'rgba(0,0,0,0.35)'
color.shadow.lg:
  light: 'rgba(0,0,0,0.12)'
  dark:  'rgba(0,0,0,0.50)'
color.shadow.xl:
  light: 'rgba(0,0,0,0.16)'
  dark:  'rgba(0,0,0,0.60)'

# ── INTERACTION ──────────────────────────────────────
color.selection:
  light: 'rgba(0,0,0,0.08)'
  dark:  'rgba(235,235,240,0.15)'
color.focus:
  light: 'rgba(0,0,0,0.12)'
  dark:  'rgba(235,235,240,0.20)'
color.ripple:
  light: 'rgba(0,0,0,0.06)'
  dark:  'rgba(235,235,240,0.10)'
color.hover:
  light: 'rgba(0,0,0,0.04)'
  dark:  'rgba(235,235,240,0.06)'
color.pressed:
  light: 'rgba(0,0,0,0.08)'
  dark:  'rgba(235,235,240,0.12)'

# ── SKELETON ─────────────────────────────────────────
color.skeleton.base:
  light: '#F3F3F3'
  dark:  '#232325'
color.skeleton.highlight:
  light: '#EBEBEB'
  dark:  '#2A2A2D'

# ── NAVIGATION ───────────────────────────────────────
color.nav.icon.selected:
  light: '#000000'
  dark:  '#EBEBF0'
color.nav.icon.inactive:
  light: 'rgba(0,0,0,0.30)'
  dark:  '#636366'
color.nav.label.selected:
  light: '#000000'
  dark:  '#EBEBF0'
color.nav.label.inactive:
  light: 'rgba(0,0,0,0.30)'
  dark:  '#636366'
color.nav.badge.background:
  light: '#FF3B30'
  dark:  '#FF6B6B'
color.nav.badge.foreground:
  light: '#FFFFFF'
  dark:  '#FFFFFF'

# ── CAROUSEL ─────────────────────────────────────────
color.indicator.active:
  light: '#000000'
  dark:  '#EBEBF0'
color.indicator.inactive:
  light: '#D1D1D6'
  dark:  '#48484C'

# ── SWATCHES ─────────────────────────────────────────
color.swatch.ring.selected:
  light: '#000000'
  dark:  '#EBEBF0'
color.swatch.ring.default:
  light: transparent
  dark:  transparent

# ── TABS ─────────────────────────────────────────────
color.tab.selected.background:
  light: '#000000'
  dark:  '#FFFFFF'
color.tab.selected.foreground:
  light: '#FFFFFF'
  dark:  '#111113'
color.tab.unselected.foreground:
  light: '#6E6E73'
  dark:  '#8E8E93'
color.tab.container:
  light: '#F3F3F3'
  dark:  '#1C1C1F'

# ── RADIUS (theme-independent) ───────────────────────
radius.xs:   8
radius.sm:   12
radius.md:   16
radius.lg:   20
radius.xl:   24
radius.2xl:  28
radius.full: 9999

# ── SPACING (theme-independent) ──────────────────────
space.4:   4
space.8:   8
space.12: 12
space.16: 16
space.20: 20
space.24: 24
space.32: 32
space.40: 40
space.48: 48

# ── ELEVATION ────────────────────────────────────────
elevation.none:
  light: 'none'
  dark:  'none'
elevation.xs:
  light: '0 2px 8px rgba(0,0,0,0.06)'
  dark:  '0 2px 8px rgba(0,0,0,0.20)'
elevation.sm:
  light: '0 4px 18px rgba(0,0,0,0.08)'
  dark:  '0 4px 12px rgba(0,0,0,0.25)'
elevation.md:
  light: '0 8px 24px rgba(0,0,0,0.10)'
  dark:  '0 8px 20px rgba(0,0,0,0.35)'
elevation.lg:
  light: '0 12px 32px rgba(0,0,0,0.12)'
  dark:  '0 16px 40px rgba(0,0,0,0.50)'
elevation.xl:
  light: '0 16px 40px rgba(0,0,0,0.16)'
  dark:  '0 20px 48px rgba(0,0,0,0.60)'

# ── TYPOGRAPHY COLORS ─────────────────────────────────
typography.color.primary:
  light: '#111111'
  dark:  '#EBEBF0'
typography.color.secondary:
  light: '#6E6E73'
  dark:  '#AEAEB2'
typography.color.tertiary:
  light: '#8E8E93'
  dark:  '#8E8E93'
typography.color.disabled:
  light: '#B4B4B8'
  dark:  '#636366'
typography.color.inverse:
  light: '#FFFFFF'
  dark:  '#111113'
typography.color.caption:
  light: '#6E6E73'
  dark:  '#8E8E93'
typography.color.hint:
  light: '#B7B7B7'
  dark:  '#636366'
typography.color.error:
  light: '#FF3B30'
  dark:  '#FF6B6B'
typography.color.success:
  light: '#34C759'
  dark:  '#34D058'
typography.color.warning:
  light: '#FF9500'
  dark:  '#FFB347'
typography.color.link:
  light: '#007AFF'
  dark:  '#6B9FFF'
typography.color.price:
  light: '#111111'
  dark:  '#EBEBF0'
typography.color.badge:
  light: '#111111'
  dark:  '#EBEBF0'

# ── MOTION (theme-independent) ───────────────────────
motion.duration.fast:    '150ms'
motion.duration.normal:  '200ms'
motion.duration.slow:    '300ms'
motion.easing.default:   'cubic-bezier(0.0, 0.0, 0.2, 1.0)'
motion.scale.press:      '0.96'
motion.scale.card.press: '0.98'
motion.scale.wishlist:   '1.10'
```

---

# 18. AI Implementation Rules

## General (Both Themes)

- Prefer shadows over borders
- Generous whitespace — 8pt grid always
- All interactive controls: rounded corners
- Monochromatic UI — product imagery supplies color
- Visual hierarchy via spacing, not font size inflation
- No visual clutter — no unnecessary icons, labels, decorative elements
- Navigation lightweight, secondary to product content
- One primary CTA per screen — maximum

## Layout Rules

- Safe areas respected on all screens
- Horizontal padding: **20dp**
- Vertical spacing between major sections: **24-32dp**
- Card padding: **16dp**

## Light-Specific Rules

- Background stays `#FFFFFF` — never off-white for main canvas
- Cards float via shadow Y:4 Blur:18 opacity 8%
- Primary button = `#000000` fill

## Dark-Specific Rules

- **Never** use `#000000` as primary background (non-OLED mode)
- **Always** use the 5-layer surface system — each level visibly distinguishable
- **Never** add borders to cards — luminance separation mandatory
- **Always** use `#EBEBF0` for primary text — never `#FFFFFF`
- **Never** tint or desaturate product images
- **Always** use explicit disabled colors — never `alpha(0.38)`
- **Never** colored neon glows — ambient glow is white-tinted only
- **Always** pair status states with icon — never color alone
- **Never** large pure-white surfaces in dark screens (exception: primary button)

## Component Rules

- Buttons pill-shaped, minimum height **52dp**
- Cards **24dp** radius, subtle elevation, no border
- Product images **60-70%** of card container
- Icons: rounded strokes **20-24dp**
- Color selectors: circular, outlined selected state
- Inputs: filled backgrounds

## Anti-Patterns

Must avoid:
- Heavy shadows
- Thick borders
- Sharp corners
- Dense layouts
- Saturated UI colors
- Multiple competing accent colors
- Excessive typography weights
- More than one primary CTA per screen
- Inconsistent spacing or radius
- Decorative gradients (unless explicitly requested)
- **(Dark)** Colored glows / neon
- **(Dark)** Same-color adjacent surfaces
- **(Dark)** Opacity-based disabled states

---

# 19. Do & Don't Guidelines

## Backgrounds

| Do | Don't |
|---|---|
| Light: `#FFFFFF` canvas | Light: off-white canvas |
| Dark: `#111113` near-black | Dark: `#000000` pure black |
| Dark: layer surfaces via luminance | Dark: same color on adjacent surfaces |
| Dark: OLED black only in OLED mode | Dark: OLED black as default |

## Text

| Do | Don't |
|---|---|
| Dark: `#EBEBF0` primary text | Dark: `#FFFFFF` primary text |
| Rely on luminance for hierarchy | Bold secondary text for emphasis |
| Soft status colors `#FF6B6B` | Pure alarm red `#FF0000` |

## Cards

| Do | Don't |
|---|---|
| Light: float via shadow / Dark: float via luminance | Heavy drop shadows |
| Image well darker than card | White/bright image well in dark |
| 16dp padding | Cramped padding |
| No card borders | Add borders to cards |

## Buttons

| Do | Don't |
|---|---|
| One primary button per screen | Multiple primary buttons |
| Light: `#000000` fill | Light: colored primary button |
| Dark: `#FFFFFF` fill | Dark: gray or dimmed primary button |
| Explicit disabled colors | `opacity(0.38)` for disabled |
| Scale 96% on press | Color-only press feedback |

## Icons & Imagery

| Do | Don't |
|---|---|
| Stroke icons only | Mix fill + stroke |
| Products at full saturation | Dark overlay on product images |
| Pair status with icon | Status via color alone |

## Navigation

| Do | Don't |
|---|---|
| Nav matches canvas level | Bright contrasting nav color |
| Dark: hairline top border on bottom nav | Thick visible separator |
| Dark: white FAB | Colored/tinted FAB |

---

# 20. Migration — Light to Dark

## Token Mapping

| Token | Light | Dark | Notes |
|---|---|---|---|
| `background.primary` | `#FFFFFF` | `#111113` | Not inverted — designed separately |
| `background.secondary` | `#F8F8F8` | `#161618` | |
| `surface.card` | `#FFFFFF` | `#232325` | Own distinct surface in dark |
| `text.primary` | `#111111` | `#EBEBF0` | Off-white, not pure white |
| `text.secondary` | `#6E6E73` | `#AEAEB2` | Lightened |
| `text.disabled` | `#B4B4B8` | `#636366` | Darkened |
| `border.default` | `#E7E7E7` | `#38383C` | Inverted tone |
| `action.primary.bg` | `#000000` | `#FFFFFF` | **Inverted** |
| `action.primary.fg` | `#FFFFFF` | `#111113` | **Inverted** |
| `action.secondary.bg` | `#F3F3F3` | `#2A2A2D` | |
| `surface.input` | `#F8F8F8` | `#313134` | |
| `success` | `#34C759` | `#34D058` | Slightly brightened |
| `error` | `#FF3B30` | `#FF6B6B` | Desaturated — softer on dark |
| `warning` | `#FF9500` | `#FFB347` | Warmer amber |
| `icon.default` | `#222222` | `#EBEBF0` | |
| `skeleton.base` | `#F3F3F3` | `#232325` | |
| `indicator.active` | `#000000` | `#EBEBF0` | |
| `swatch.ring.selected` | `#000000` | `#EBEBF0` | |

## Key Behavioral Changes

1. **Primary button inverts** — black to white. Intentional. Correct.
2. **Cards: no border** in both themes — shadows (light) / luminance (dark).
3. **Dark shadows near-invisible** — rely on surface color steps.
4. **Bottom nav separator** adds `rgba(235,235,240,0.08)` hairline in dark.
5. **Carousel dots** flip: `#000000` active to `#EBEBF0`, `#D1D1D6` inactive to `#48484C`.
6. **Tab selected** flips: black pill + white text to white pill + dark text.
7. **Error red** softened `#FF3B30` to `#FF6B6B`.
8. **Rating stars** warm amber `#FFB347` in dark.
9. **Image placeholder/skeleton** white to dark `#232325`.
10. **Quantity button** gains `1px #38383C` border in dark.

## Kotlin / Compose Implementation

Place in `:presentation/common/theme/`:

```kotlin
// LightColors.kt
object LightColors {
    val BackgroundPrimary   = Color(0xFFFFFFFF)
    val BackgroundSecondary = Color(0xFFF8F8F8)
    val SurfaceCard         = Color(0xFFFFFFFF)
    val SurfaceInput        = Color(0xFFF8F8F8)
    val TextPrimary         = Color(0xFF111111)
    val TextSecondary       = Color(0xFF6E6E73)
    val TextDisabled        = Color(0xFFB4B4B8)
    val ActionPrimary       = Color(0xFF000000)
    val ActionPrimaryFg     = Color(0xFFFFFFFF)
    val Error               = Color(0xFFFF3B30)
    val Success             = Color(0xFF34C759)
    val Warning             = Color(0xFFFF9500)
    val BorderDefault       = Color(0xFFE7E7E7)
    val Divider             = Color(0xFFEEEEEE)
}

// DarkColors.kt
object DarkColors {
    val BackgroundPrimary   = Color(0xFF111113)
    val BackgroundSecondary = Color(0xFF161618)
    val BackgroundTertiary  = Color(0xFF1C1C1F)
    val BackgroundOled      = Color(0xFF0A0A0B)
    val SurfaceCard         = Color(0xFF232325)
    val SurfaceElevated     = Color(0xFF2A2A2D)
    val SurfaceInput        = Color(0xFF313134)
    val TextPrimary         = Color(0xFFEBEBF0)
    val TextSecondary       = Color(0xFFAEAEB2)
    val TextTertiary        = Color(0xFF8E8E93)
    val TextDisabled        = Color(0xFF636366)
    val TextInverse         = Color(0xFF111113)
    val ActionPrimary       = Color(0xFFFFFFFF)
    val ActionPrimaryFg     = Color(0xFF111113)
    val ActionSecondary     = Color(0xFF2A2A2D)
    val Error               = Color(0xFFFF6B6B)
    val Success             = Color(0xFF34D058)
    val Warning             = Color(0xFFFFB347)
    val Info                = Color(0xFF6B9FFF)
    val BorderDefault       = Color(0xFF38383C)
    val BorderFocus         = Color(0xFFEBEBF0)
    val Divider             = Color(0xFF2A2A2D)
    val Ripple              = Color(0x1AEBEBF0)
    val Overlay             = Color(0x99000000)
    val SkeletonBase        = Color(0xFF232325)
    val SkeletonHighlight   = Color(0xFF2A2A2D)
}
```

Select in `ShopzenTheme.kt`:

```kotlin
@Composable
fun ShopzenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) darkColorScheme(...) else lightColorScheme(...)
    MaterialTheme(colorScheme = colors, content = content)
}
```

> Override Material 3 tonal elevation tinting — Shopzen uses manual surface system. Set `LocalAbsoluteTonalElevation` to zero and control surfaces via token objects.

---

# Overall Visual Identity

Both themes must feel:

| Quality | Description |
|---|---|
| **Premium** | Calm, expensive, intentional |
| **Minimal** | No noise, no clutter |
| **Spacious** | Identical rhythm in both themes |
| **Product-first** | UI disappears, products pop |
| **Apple-caliber** | Measured, quiet elegance |
| **Accessible** | WCAG AA minimum everywhere |
| **Consistent** | Same language every screen, every state |
| **Touch-optimized** | 44dp minimum, large CTAs |
| **OLED-friendly** | Near-black backgrounds save battery (dark) |
| **Soft yet high-contrast** | Readable without harshness |
