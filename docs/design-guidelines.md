# Sinsay Design Guidelines

> Extracted from https://www.sinsay.com/pl/pl — 2026-03-27

This document describes the visual design system of Sinsay and should be used as the reference for building our application to maintain brand consistency.

## Assets

| Asset | File |
|---|---|
| Homepage screenshot | [assets/sinsay-homepage.png](../assets/sinsay-homepage.png) |
| Wordmark (SVG) | [assets/logo.svg](../assets/logo.svg) |
| Favicon (ICO) | [assets/sinsay-favicon.ico](../assets/sinsay-favicon.ico) |
| Design tokens (JSON) | [assets/design-tokens.json](../assets/design-tokens.json) |

---

## Colors

### Brand Palette

| Token | Hex | Usage |
|---|---|---|
| `brand.primary` | `#16181D` | Wordmark, headings, dark text |
| `brand.accent` | `#E09243` | Primary CTA buttons, highlights |
| `brand.error` | `#E90000` | Promotional announcement bar, errors |
| `brand.success` | `#0BB407` | Online/available indicators |

### Text Colors

| Token | Hex | Usage |
|---|---|---|
| `text.primary` | `#333333` | Body text, most content |
| `text.secondary` | `#777777` | Secondary/supporting text |
| `text.muted` | `#909090` | Disabled, placeholder text |
| `text.dark` | `#16181D` | Heavy emphasis |
| `text.nav` | `#303133` | Navigation links |
| `text.onDark` | `#FFFFFF` | Text on dark/colored backgrounds |

### Background Colors

| Token | Hex | Usage |
|---|---|---|
| `bg.default` | `#FFFFFF` | Page background, header, footer |
| `bg.light` | `#EEF9FC` | Light blue tinted sections |
| `bg.overlay` | `rgba(0,0,0,0.4)` | Modal overlays |

---

## Typography

### Font Family

The site uses **Euclid Circular B** — a custom geometric sans-serif font. Use the following stack:

```css
font-family: "Euclid Circular B", Arial, Helvetica, "Helvetica Neue", sans-serif;
```

In the site's CSS it is aliased as `Euclid`:

```css
@font-face {
  font-family: Euclid;
  src: url("fonts/euclid/EuclidCircularB-Regular-WebXL.woff") format("woff");
  font-weight: 400;
  font-display: swap;
}
```

Available weights: **300** (Light), **400** (Regular), **500** (Medium), **600** (Semibold), **700** (Bold).

### Type Scale

| Role | Size | Weight | Line-height |
|---|---|---|---|
| Body | 16px | 400 | 1.43 |
| Body small | 14px | 400 | 1.43 |
| Caption | 13px | 400 | 1.5 |
| Heading 1 | 24px | 600 | 1.0 |
| Nav link | 16px | 400 | 1.25 |
| Button | 16px | 600 | 1.5 |
| Header/Nav | 14px | 400 | 1.43 |

---

## Spacing

Base spacing unit: **4px**. Common values observed on the site:

| Token | Value | Usage |
|---|---|---|
| `spacing.2` | 8px | Small gaps |
| `spacing.3` | 12px | Button vertical padding |
| `spacing.4` | 16px | Component margin |
| `spacing.5` | 20px | Section padding |
| `spacing.6` | 24px | Large gaps |
| `spacing.7` | 28px | Header horizontal padding |
| `spacing.8` | 32px | Button horizontal padding |

---

## Border Radius

Sinsay uses a mix of sharp corners (0px) for structural elements and rounded values for inputs/chips:

| Token | Value | Usage |
|---|---|---|
| `radius.none` | `0px` | Buttons, cards, images |
| `radius.sm` | `4px` | Small UI elements |
| `radius.md` | `34px` | Search bar, pill inputs |
| `radius.lg` | `50px` | Rounded pill tags |
| `radius.full` | `999px` | Full pill elements |
| `radius.circle` | `50%` | Avatar/category circles |

---

## Components

### Header

- White background (`#FFFFFF`)
- 1px bottom border (light gray)
- Horizontal padding: `0 28px`
- Contains: country selector (left), wordmark logo (center), search bar + icons (right)
- Font: 14px / 400 / `#333333`

### Announcement / Promo Bar

- Background: `#E90000` (red)
- Text: white, bold (700), 14px
- Full-width, positioned above the header

### Primary CTA Button

```css
background-color: #E09243;
color: #FFFFFF;
border: 1.6px solid #E09243;
border-radius: 0px;
padding: 12px 32px;
font-size: 16px;
font-weight: 600;
line-height: 24px;
```

### Navigation Links

```css
color: #303133;
font-size: 16px;
font-weight: 400;
text-decoration: none;
text-transform: none;
```

### Search Input

- Border-radius: `34px` (pill shape)
- Font size: `14px`
- Background: white with light border

### Category Circles

- Images in circular crop (`border-radius: 50%`)
- Label below in 13px / 400 / `#333333`

---

## Logo / Wordmark

The Sinsay wordmark is a custom SVG path (`fill="#16181D"`), 84×31px viewport. File: [assets/logo.svg](../assets/logo.svg).

On colored/dark backgrounds, invert the fill to `#FFFFFF`.

The favicon is an "S" lettermark in a dark square container. File: [assets/sinsay-favicon.ico](../assets/sinsay-favicon.ico).

---

## Layout

- Max content width: ~1440px
- Grid: standard 12-column
- Product cards: no border-radius, white background
- Section spacing: 40–60px vertical gaps between sections

---

## Visual Style Summary

Sinsay follows a **clean, minimalist fashion retail** aesthetic:

- **Sharp corners** on buttons and cards (no border-radius on key UI elements) — conveys modern, editorial feel
- **Minimal color** — mostly white/gray with selective use of the orange accent and red for urgency
- **Heavy use of photography** — product imagery dominates the layout
- **Euclid Circular B** gives a geometric, contemporary brand feel
- **Uppercase is avoided** — navigation and labels use sentence case / title case
