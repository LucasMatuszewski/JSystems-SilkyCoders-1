# Sinsay Design System — Technical Style Guide

> **Audit date:** 2026-02-24
> **Source:** https://www.sinsay.com/pl/pl/ (live production site)
> **Method:** Playwright browser automation — computed styles, CSS custom properties, DOM inspection
> **Total CSS variables extracted:** 1 087

---

## Table of Contents

1. [Logo & Brand Assets](#1-logo--brand-assets)
2. [Color Palette](#2-color-palette)
3. [Typography](#3-typography)
4. [Spacing Scale](#4-spacing-scale)
5. [Border Radius](#5-border-radius)
6. [Borders & Outlines](#6-borders--outlines)
7. [Elevation & Shadows](#7-elevation--shadows)
8. [Breakpoints & Layout Grid](#8-breakpoints--layout-grid)
9. [Animation & Transitions](#9-animation--transitions)
10. [Component Specs](#10-component-specs)
    - [10.1 Header / Navigation](#101-header--navigation)
    - [10.2 Promotional Bars](#102-promotional-bars)
    - [10.3 Search Bar](#103-search-bar)
    - [10.4 Product Tile (Listing Card)](#104-product-tile-listing-card)
    - [10.5 Product Detail Page (PDP)](#105-product-detail-page-pdp)
    - [10.6 Badges & Stickers](#106-badges--stickers)
    - [10.7 Buttons](#107-buttons)
    - [10.8 Form Inputs](#108-form-inputs)
    - [10.9 Category Circles](#109-category-circles)
    - [10.10 Footer](#1010-footer)
11. [CSS Custom Properties Reference](#11-css-custom-properties-reference)

---

## 1. Logo & Brand Assets

### Files
| File | Usage |
|------|-------|
| `docs/assets/sinsay-logo.svg` | Default logo — dark `#16181D` fill, use on white/light backgrounds |
| `docs/assets/sinsay-logo-white.svg` | Inverted logo — white fill, use on dark/coloured backgrounds |

### Specifications
- **Viewbox:** `0 0 84 31`
- **Rendered size (desktop header):** `84 × 31 px`
- **Fill colour (dark version):** `#16181D`
- **Fill colour (light version):** `#FFFFFF`
- The wordmark is a custom geometric sans-serif. The `i` letterform has a distinctive separated dot above a tall vertical stroke (visible in the SVG path).

### Usage Rules
```html
<!-- Standard header usage -->
<a href="/" class="brand-logo-button">
  <svg xmlns="http://www.w3.org/2000/svg" width="84" height="31" viewBox="0 0 84 31" fill="none">
    <!-- paths from docs/assets/sinsay-logo.svg -->
  </svg>
</a>
```

---

## 2. Color Palette

All colours are defined as CSS custom properties on `:root`. The system uses **two naming conventions** that coexist:
- **Legacy tokens** — `--color-*` (prefix without `s`)
- **Current tokens** — `--colors-*` (plural, canonical) + `--dt-color-*` (design-token system, newer)

### 2.1 Primary — Warm Orange

| Token | Hex | Preview |
|-------|-----|---------|
| `--colors-primary-90` | `#1A0D00` | Darkest |
| `--colors-primary-80` | `#4D2600` | |
| `--colors-primary-70` | `#804306` | |
| `--colors-primary-60` | `#B2671B` | |
| **`--colors-primary-50`** | **`#E09243`** | **Default / brand accent** |
| `--colors-primary-40` | `#F2B06D` | |
| `--colors-primary-30` | `#F2C291` | |
| `--colors-primary-20` | `#FFDFBF` | |
| `--colors-primary-10` | `#FFF2E5` | Lightest |

```css
--color-primary: var(--color-primary-50); /* #E09243 */
```

### 2.2 Neutral — Dark / Grey Scale

| Token | Hex |
|-------|-----|
| `--colors-dark-100` | `#000000` |
| `--colors-dark-90` | `#18191A` — brand near-black (used for body text, CTA bg) |
| `--colors-dark-80` | `#303133` |
| `--colors-dark-70` | `#494A4D` |
| `--colors-dark-60` | `#616366` |
| `--colors-dark-50` | `#7B7D80` |
| `--colors-dark-40` | `#949699` |
| `--colors-dark-30` | `#AFB0B2` |
| `--colors-dark-20` | `#C8C9CC` — border default |
| `--colors-dark-10` | `#E3E4E5` — background grey |
| `--colors-dark-5`  | `#F1F2F4` — subtle background |
| `--colors-white-100` | `#FFFFFF` |

### 2.3 Semantic Colours

| Colour | 50 (default) | 60 | Notes |
|--------|--------------|----|-------|
| **Red** | `#FF0023` | `#CC001C` | Sale prices, error states, wishlist |
| **Green** | `#0DB209` | `#0A8C07` | Success, eco label, costless |
| **Blue** | `#2E90E5` | `#1268B2` | Info states |
| **Orange** | `#FF9900` | `#CC7A00` | Alerts, Sinsay Club badge |

### 2.4 Alpha Overlays

```css
/* Dark overlays */
--colors-alpha-dark-90: rgba(0,0,0,0.9)
--colors-alpha-dark-80: rgba(0,0,0,0.8)
--colors-alpha-dark-50: rgba(0,0,0,0.5)  /* standard overlay */
--colors-alpha-dark-20: rgba(0,0,0,0.2)  /* product card overlay */
--colors-alpha-dark-10: rgba(0,0,0,0.1)
--colors-alpha-dark-5:  rgba(0,0,0,0.05)

/* White overlays */
--colors-alpha-white-90: rgba(255,255,255,0.9)
--colors-alpha-white-50: rgba(255,255,255,0.5)
--colors-alpha-white-20: rgba(255,255,255,0.2)
```

### 2.5 Semantic Token Map (colour-tokens layer)

The `--color-tokens-*` namespace maps semantic intent to palette values:

```css
/* Backgrounds */
--color-tokens-background-base-white:        var(--colors-white-100)
--color-tokens-background-base-gray-light:   var(--colors-dark-5)
--color-tokens-background-base-gray:         var(--colors-dark-10)
--color-tokens-background-base-accent:       var(--colors-primary-50)
--color-tokens-background-base-dark:         var(--colors-dark-90)
--color-tokens-background-base-overlay:      rgba(0,0,0,0.2)

/* CTA button backgrounds */
--color-tokens-background-controls-button-primary-default-default: var(--colors-primary-50)
--color-tokens-background-controls-button-primary-default-hover:   var(--colors-primary-60)
--color-tokens-background-controls-button-primary-default-disabled: var(--colors-primary-20)
--color-tokens-background-controls-button-primary-dark-default:    var(--colors-dark-90)
--color-tokens-background-controls-button-primary-dark-hover:      var(--colors-dark-70)

/* Text */
--color-tokens-text-base-dark-dark:          var(--colors-dark-90)
--color-tokens-text-base-dark-high-contrast: var(--colors-dark-80)
--color-tokens-text-base-dark-low-contrast:  var(--colors-dark-60)
--color-tokens-text-base-dark-disabled:      var(--colors-dark-30)
--color-tokens-text-special-accent:          var(--colors-primary-50)
--color-tokens-text-product-categories-sale: var(--colors-red-50)
--color-tokens-text-product-categories-eco:  #63A73A

/* Borders */
--color-tokens-border-base-regular:          var(--colors-dark-10)
--color-tokens-border-base-dark:             var(--colors-dark-90)
--color-tokens-border-controls-dark-default: var(--colors-dark-20)
--color-tokens-border-controls-dark-active:  var(--colors-primary-50)
--color-tokens-border-base-validation:       var(--colors-red-50)
--color-tokens-border-base-focus:            var(--colors-primary-50)
```

### 2.6 Additional Extended Palette (dt-color system)

Additional named colour families used in newer components:

| Family | 50 | Notes |
|--------|----|-------|
| Tomato | `#DC371C` | Alternative red/CTA variant |
| Violet | `#6B6AD8` | Promotional purple-ish |
| Grass  | `#4A862D` | Eco/sustainability |
| Moss   | `#447B3B` | Eco alternative |
| Sky    | — (100: `#DAE6FF`) | Light informational |

### 2.7 Observed Contextual Colours (from computed styles)

| Context | Value |
|---------|-------|
| Body text | `rgb(0,0,0)` / `#000000` |
| Navigation link text | `rgb(48,49,51)` / `#303133` |
| Product name text | `rgb(22,24,29)` / `#16181D` |
| Secondary text | `rgb(106,107,110)` / `#6A6B6E` |
| Promo sticker — purple | `rgb(172,119,194)` / `#AC77C2` |
| Sale sticker background | `rgb(217,46,49)` / `#D92E31` |
| Sale sticker text | `#FFFFFF` |
| Hot badge background | `rgb(255,222,222)` / `#FFDEDE` |
| Hot badge text | `rgb(244,61,61)` / `#F43D3D` |
| Input border | `rgb(199,200,201)` / `#C7C8C9` |
| Footer text | `rgb(35,35,35)` / `#232323` |
| Brand dark (used in logo, CTA) | `rgb(22,24,29)` / `#16181D` |

---

## 3. Typography

### 3.1 Font Family

```css
--font-family-base:       'Euclid Circular B'
--dt-font-family-base:    'Euclid Circular B'
--dt-font-family-heading: 'Euclid Circular B'
--dt-font-family-support: sans-serif, Helvetica, Arial

/* Computed body font-family (browser fallback chain) */
font-family: Euclid, Arial, Helvetica, "Helvetica Neue", sans-serif;
```

> **Euclid Circular B** is a commercial typeface by Swiss Typefaces. It is the sole typeface used across all text elements. Weights used: 400, 500, 600, 700.

### 3.2 Font Size Scale

#### Heading Scale
| Token | Value | Usage |
|-------|-------|-------|
| `--font-size-heading-mega` | `80px` | Hero/campaign typography |
| `--font-size-heading-xl`   | `48px` | Major section headings |
| `--font-size-heading-l`    | `32px` | Page headings |
| `--font-size-heading-m`    | `24px` | Section headings |
| `--font-size-heading-s`    | `20px` | Card headings |

#### Body Scale
| Token | Value | Equivalent rem | Usage |
|-------|-------|----------------|-------|
| `--font-size-default-xl` | `18px` | `1.125rem` | Large body |
| `--font-size-default-l`  | `16px` | `1rem`     | Body default (base) |
| `--font-size-base`       | `16px` | `1rem`     | Root base |
| `--font-size-default-m`  | `14px` | `0.875rem` | Secondary text, UI labels |
| `--font-size-default-s`  | `12px` | `0.75rem`  | Captions, badges |
| `--font-size-default-xs` | `10px` | `0.625rem` | Fine print |

#### dt-system Granular Scale (rem-based)
```css
--dt-font-size-100: 0.5rem     /* 8px  */
--dt-font-size-125: 0.625rem   /* 10px */
--dt-font-size-150: 0.75rem    /* 12px */
--dt-font-size-175: 0.875rem   /* 14px */
--dt-font-size-200: 1rem       /* 16px */
--dt-font-size-225: 1.125rem   /* 18px */
--dt-font-size-250: 1.25rem    /* 20px */
--dt-font-size-300: 1.5rem     /* 24px */
--dt-font-size-350: 1.75rem    /* 28px */
--dt-font-size-400: 2rem       /* 32px */
--dt-font-size-500: 2.5rem     /* 40px */
--dt-font-size-600: 3rem       /* 48px */
```

### 3.3 Font Weight

| Token | Value | Usage |
|-------|-------|-------|
| `--font-weight-regular`  / `--dt-font-weight-normal`  | `400` | Body, nav links, product names |
| `--font-weight-medium`   / `--dt-font-weight-medium`  | `500` | Prices, emphasis |
| `--font-weight-semibold` / `--dt-font-weight-bold`    | `600` | Footer headings, subheadings |
| `--font-weight-bold`     / `--dt-font-weight-strong`  | `700` | Display / hero text |

### 3.4 Line Height

| Token | Value | Usage |
|-------|-------|-------|
| `--dt-font-line-height-tight`   | `120%` | Headings, display |
| `--dt-font-line-height-compact` | `130%` | Sub-headings |
| `--dt-font-line-height-default` | `150%` | Body text |

### 3.5 Letter Spacing

| Token | Value | Usage |
|-------|-------|-------|
| `--dt-font-spacing-narrow`    | `-0.0312rem` (`-0.5px`) | Tighter headings |
| `--dt-font-spacing-narrowed`  | `-0.0156rem` (`-0.25px`) | Slight tightening |
| `--dt-font-spacing-default`   | `0rem` | Default |
| `--font-h-space-xl`           | `1px` | Very wide tracking |
| `--font-h-space-l`            | `0.8px` | |
| `--font-h-space-m`            | `0.6px` | |
| `--font-h-space-sm`           | `0.3px` | |
| `--font-h-space-s`            | `0.2px` | |
| `--font-h-space-xs`           | `0.1px` | Minimal |

### 3.6 Text Decoration & Case

```css
--text-case-upper:   uppercase
--text-case-default: none
--text-decor-link:   underline
--text-decor-discount: line-through   /* strikethrough for original prices */
```

### 3.7 Observed Typography (Computed Styles)

| Element | Font Size | Weight | Color | Line Height |
|---------|-----------|--------|-------|-------------|
| Body | `16px` | `400` | `#000000` | `16px` |
| Nav links | `16px` | `400` | `#303133` | — |
| Product name (PDP) | `13px` | `400` | `#000000` | `16.9px` |
| Product tile price | `12px` | `500` | `#000000` | `15.6px` |
| Footer heading | `14px` | `600` | `#232323` | — |
| Footer links | `16px` | `400` | `#303133` | — |
| Category label | `14px` | `400` | `#16181D` | `18.2px` |
| Breadcrumb | `14px` | `400` | `#000000` | — |
| Sticker badges | `14px` | `400` | varies | — |
| Hot badge | `8px` | `500` | `#F43D3D` | — |
| Sale discount text | `10px` | `500` | `#FFFFFF` | — |

---

## 4. Spacing Scale

Sinsay uses a **base-8 spacing system** with named tokens and a numeric rem scale.

### 4.1 Named Base Scale

| Token | Value |
|-------|-------|
| `--size-s` / `--spacing-base-space-s` | `4px` |
| `--size-sm` / `--spacing-base-space-sm` | `8px` |
| `--size-m` / `--spacing-base-space-m` | `16px` |
| `--size-l` / `--spacing-base-space-l` | `32px` |
| `--size-xl` / `--spacing-base-space-xl` | `64px` |
| `--size-xxl` / `--spacing-base-space-xxl` | `128px` |

### 4.2 Extended Other Sizes

| Token | Value |
|-------|-------|
| `--spacing-other-space-2` | `2px` |
| `--spacing-other-space-12` | `12px` |
| `--spacing-other-space-24` | `24px` |
| `--spacing-other-space-40` | `40px` |
| `--spacing-other-space-48` | `48px` |
| `--spacing-other-space-56` | `56px` |
| `--spacing-other-space-72` | `72px` |
| `--spacing-other-space-80` | `80px` |
| `--spacing-other-space-88` | `88px` |

### 4.3 dt-system Spacing (rem-based)

```css
--dt-space-0:    0rem
--dt-space-13:   0.0625rem   /* 1px  */
--dt-space-25:   0.125rem    /* 2px  */
--dt-space-37:   0.1875rem   /* 3px  */
--dt-space-50:   0.25rem     /* 4px  */
--dt-space-75:   0.375rem    /* 6px  */
--dt-space-100:  0.5rem      /* 8px  */
--dt-space-125:  0.625rem    /* 10px */
--dt-space-150:  0.75rem     /* 12px */
--dt-space-175:  0.875rem    /* 14px */
--dt-space-200:  1rem        /* 16px */
--dt-space-250:  1.25rem     /* 20px */
--dt-space-300:  1.5rem      /* 24px */
--dt-space-350:  1.75rem     /* 28px */
--dt-space-400:  2rem        /* 32px */
--dt-space-500:  2.5rem      /* 40px */
--dt-space-600:  3rem        /* 48px */
--dt-space-700:  3.5rem      /* 56px */
--dt-space-800:  4rem        /* 64px */
--dt-space-900:  4.5rem      /* 72px */
--dt-space-1000: 5rem        /* 80px */
--dt-space-1100: 5.5rem      /* 88px */
--dt-space-1200: 6rem        /* 96px */
--dt-space-1600: 8rem        /* 128px */
```

### 4.4 Layout Gutters

| Context | Value |
|---------|-------|
| Desktop gutter | `24px` (`--layout-gutter-base-desktop`) |
| Tablet gutter | `24px` (`--layout-gutter-base-tablet`) |
| Mobile gutter | `16px` (`--layout-gutter-base-mobile`) |
| Desktop large margin | `64px` |
| Desktop small margin | `32px` |
| Tablet margin | `24px` |
| Mobile margin | `16px` |

---

## 5. Border Radius

### 5.1 Base (Square) Radii

| Token | Value | Usage |
|-------|-------|-------|
| `--radius-base-xs` | `2px` | Subtle rounding |
| `--radius-base-s`  | `4px` | Tags, input corners |
| `--radius-base-sm` | `8px` | Cards |
| `--radius-base-m`  | `16px` | Modals, panels |
| `--radius-base-l`  | `32px` | Large cards |

### 5.2 Pill Radii

| Token | Value |
|-------|-------|
| `--radius-pill-xxs` | `8px` |
| `--radius-pill-xs`  | `8px` |
| `--radius-pill-s`   | `12px` |
| `--radius-pill-sm`  | `16px` |
| `--radius-pill-m`   | `20px` |
| `--radius-pill-l`   | `24px` |
| `--radius-pill-xl`  | `32px` |

### 5.3 Circle / Full-Pill

| Token | Value |
|-------|-------|
| `--radius-circle-s`  | `8px` |
| `--radius-circle-sm` | `12px` |
| `--radius-circle-m`  | `16px` |
| `--radius-circle-l`  | `20px` |
| `--radius-circle-xl` | `24px` |
| `--radius-circle-xxl` | `32px` |
| `--radius-circle-40` | `40px` |
| `--radius-circle-48` | `48px` |
| `--dt-radius-shape-pill` | `999px` / `62.4375rem` — full pill |
| `--dt-radius-shape-round` | `999px` — full circle |

### 5.4 dt-system Radii

```css
--dt-radius-25:  0.125rem   /* 2px  */
--dt-radius-50:  0.25rem    /* 4px  */
--dt-radius-75:  0.375rem   /* 6px  */
--dt-radius-100: 0.5rem     /* 8px  */
--dt-radius-150: 0.75rem    /* 12px */
--dt-radius-200: 1rem       /* 16px */
--dt-radius-300: 1.5rem     /* 24px */
--dt-radius-400: 2rem       /* 32px */
--dt-radius-500: 2.5rem     /* 40px */
--dt-radius-600: 3rem       /* 48px */
```

### 5.5 Observed Component Radii

| Component | Value |
|-----------|-------|
| Add-to-cart button | `999px` (full pill) |
| Search bar button | `999px` (full pill) |
| Wishlist button | `50%` (circle) |
| Color swatch buttons | `50%` (circle) |
| Input fields | `6px` |
| Hot badge | `999px` (pill) |
| Product listing card (top) | `4px 4px 0 0` |

---

## 6. Borders & Outlines

### 6.1 Border Widths

| Token | Value |
|-------|-------|
| `--dt-border-width-small`       | `1px` (0.0625rem) |
| `--dt-border-width-medium`      | `2px` (0.125rem) |
| `--dt-border-width-large`       | `3px` (0.1875rem) |
| `--dt-border-width-extra-large` | `4px` (0.25rem) |

### 6.2 Semantic Border Tokens

```css
--dt-border-default:             1px  /* standard border */
--dt-border-active:              2px  /* active/selected state */
--dt-border-active-gentle:       1px  /* gentle active indicator */
--dt-border-error:               1px  /* validation error */
--dt-border-separator:           1px  /* divider lines */
--dt-border-effect-focus-default: 2px  /* focus ring */
--dt-border-effect-focus-large:   3px  /* large focus ring */
```

### 6.3 Observed Borders

| Component | Border |
|-----------|--------|
| Text inputs / search bar | `1px solid rgb(199,200,201)` (#C7C8C9) |
| Input focus / active | `1px solid var(--colors-primary-50)` |
| Input validation error | `1px solid var(--colors-red-50)` |
| Buttons (secondary dark) | `1px solid #303133` |
| Default separator | `1px solid var(--colors-dark-10)` #E3E4E5 |

---

## 7. Elevation & Shadows

### 7.1 Legacy Elevation Scale

```css
--elevation-01: 0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.08)
--elevation-02: 0px 4px 16px rgba(24,25,26,0.10), 0px 1px 4px rgba(26,13,0,0.10)
--elevation-03: 0px 6px 24px rgba(24,25,26,0.12), 0px 1px 6px rgba(26,13,0,0.10)
--elevation-04: 0px 8px 40px rgba(24,25,26,0.14), 0px 2px 12px rgba(26,13,0,0.12)
```

### 7.2 Base Elevation Scale (refined)

```css
--elevation-base-01: 0px 1px 2px rgba(51,31,0,0.08),  0px 2px 12px rgba(24,25,26,0.08)
--elevation-base-02: 0px 1px 4px rgba(51,31,0,0.10),  0px 4px 16px rgba(24,25,26,0.10)
--elevation-base-03: 0px 1px 6px rgba(51,31,0,0.10),  0px 6px 24px rgba(24,25,26,0.12)
--elevation-base-04: 0px 2px 12px rgba(51,31,0,0.12), 0px 8px 40px rgba(24,25,26,0.14)
```

### 7.3 Special Shadow Tokens

```css
/* Validation glow (error state) */
--elevation-controls-validation-glow: drop-shadow(0px 0px 8px rgba(255,0,35,0.48))

/* dt-shadow primitives */
--dt-shadow-ambient-01-y:    0.375rem  /* 6px  */
--dt-shadow-ambient-01-blur: 1.5rem   /* 24px */
--dt-shadow-core-01-y:       0.0625rem /* 1px  */
--dt-shadow-core-01-blur:    0.375rem  /* 6px  */
--dt-shadow-focus-inner:     0.0625rem /* 1px  */
--dt-shadow-focus-outer:     0.125rem  /* 2px  */
```

---

## 8. Breakpoints & Layout Grid

### 8.1 Breakpoints

| Token | Value | Context |
|-------|-------|---------|
| `--viewport-xs` / `--layout-breakpoint-mobile` | `0px` / `320px` | Mobile |
| `--viewport-s` / `--layout-breakpoint-tablet`  | `640px` | Tablet |
| `--dt-size-media-query-tablet` | `40rem` (640px) | dt-system tablet |
| `--viewport-m` / `--layout-breakpoint-desktop-small` | `1008px` | Desktop small |
| `--dt-size-media-query-desktop-small` | `64rem` (1024px) | dt-system desktop sm |
| `--viewport-l` / `--layout-breakpoint-desktop-large` | `1540px` | Desktop large |
| `--dt-size-media-query-desktop-large` | `96.25rem` (1540px) | dt-system desktop lg |

```css
/* Recommended media query pattern */
@media (min-width: 640px)  { /* tablet   */ }
@media (min-width: 1008px) { /* desktop  */ }
@media (min-width: 1540px) { /* wide     */ }
```

### 8.2 Container Widths

| Token | Value | Usage |
|-------|-------|-------|
| `--layout-wrapper-base-fluid`  | `100%` | Full bleed |
| `--layout-wrapper-base-base`   | `1440px` | Standard container |
| `--layout-wrapper-base-wide`   | `1500px` | Wide container |
| `--layout-wrapper-base-narrow` | `1140px` | Narrow content |
| `--dt-size-container-default`  | `85rem` (1360px) | dt-system default |
| `--dt-size-container-wide`     | `90rem` (1440px) | dt-system wide |
| `--dt-size-container-narrow`   | `75rem` (1200px) | dt-system narrow |
| `--dt-size-container-compressed` | `37.5rem` (600px) | Mobile/compressed |

---

## 9. Animation & Transitions

### 9.1 Global Transition

All interactive elements use a global shorthand:
```css
transition: all;   /* shorthand — browser applies default duration/easing */
```

No specific duration or easing values are set via CSS custom properties on the live site. Convention observed is fast transitions (approx. 150–200ms ease) consistent with the utility-class approach.

### 9.2 Recommended Implementation

Based on the design system conventions:

```css
/* Standard interactive transition */
transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease, box-shadow 0.15s ease;

/* Expand/collapse panels */
transition: height 0.2s ease, opacity 0.2s ease;

/* Overlay/modal fade */
transition: opacity 0.2s ease;
```

---

## 10. Component Specs

### 10.1 Header / Navigation

```
┌──────────────────────────────────────────────────────────────────┐
│ [country flag] POLSKA      S|NSAY    [search pill] [user][♡][🛒] │
│                            (logo)                                │
├──────────────────────────────────────────────────────────────────┤
│  Polecane   Kobieta   Dom   Dziecko   Mężczyzna                  │
└──────────────────────────────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Background | `#FFFFFF` |
| Height | `60px` |
| Padding | `0 28px` |
| Position | `relative` |
| Box Shadow | `none` |
| Nav link font-size | `16px` |
| Nav link font-weight | `400` |
| Nav link color | `#303133` |
| Nav link text-transform | `none` |

**Mobile header:** Same background, reduced padding, hamburger menu replaces nav links.

### 10.2 Promotional Bars

**Top promo bar** (above header):
- Background: **purple** `#AC77C2` (promotional campaign colour — varies by campaign)
- Text: `#000000` or `#FFFFFF`
- Font size: `14px`
- Font weight: `400`
- Text-align: `center`

**Secondary promo bar** (below nav, with countdown):
- Background: same campaign purple `#AC77C2`
- Text: `#333333`
- Contains countdown timer widget
- Font size: `14px`

### 10.3 Search Bar

```css
/* Search input container */
background:    #FFFFFF;
border:        1px solid rgb(199, 200, 201);  /* #C7C8C9 */
border-radius: 999px;   /* full pill */
padding:       0 16px;
height:        40px;
font-size:     14px;
color:         #16181D;
```

```html
<button class="SearchButton-module__redesigned-search-button-container">
  <span>Szukaj</span>
  <button>Stylista</button>  <!-- AI stylist feature -->
</button>
```

### 10.4 Product Tile (Listing Card)

```
┌─────────────────┐
│                 │
│   product img   │  ← aspect-ratio: 3/4 (portrait)
│                 │
│ [♡]             │  ← wishlist btn: 36×36px circle, bg #FFF, radius 50%
└─────────────────┘
  Product name      ← 14px, weight 400, color #16181D
  Price             ← 12px, weight 500, color #000000, line-height 15.6px
  [-70% BADGE] [NEW]
```

| Property | Value |
|----------|-------|
| Card background | transparent (image fills) |
| Top border-radius | `4px 4px 0 0` |
| Border | none |
| Product name size | `14px` |
| Price size | `12px` |
| Price weight | `500` |
| Wishlist button radius | `50%` |
| Wishlist button bg | `#FFFFFF` |
| Wishlist button size | approx. `36×36px` |

**Product grid:** 2 columns on mobile, 4 columns on desktop, gutter `4px` (`--layout-gutter-special-products-view`).

### 10.5 Product Detail Page (PDP)

```
┌──────────────────────────────────┐
│       Product Image Gallery      │
│       (full width, portrait)     │
│                                  │
│  "Ktoś kupił w ciągu ostatniej1h"│  ← social proof pill
│                          [1/2]   │  ← image counter
└──────────────────────────────────┘

25,99 PLN                            ← large price
[+26 pkt Sinsay Club]                ← club badge (orange)
Bawełniana koszulka basic            ← product name h1
[-70% NA TRZECI PRODUKT] [NOWOŚĆ]    ← sticker badges

Kolor: czarny                        ← label 14px #16181D
[○][○][●][○]                         ← color swatches 24×24px circle

Rozmiar              Tabela rozmiarów →
[XS ✉] [S] [M] [L] [XL ✉]           ← size chips

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
[♡]  [    Dodaj do koszyka    ]       ← sticky bottom bar
           25,99 PLN
```

**Add to cart button:**
| Property | Value |
|----------|-------|
| Background | `rgb(22,24,29)` → `#16181D` |
| Color | `#FFFFFF` |
| Border-radius | `999px` (full pill) |
| Padding | `0 20px` |
| Font-size | `14px` |
| Font-weight | `400` |
| Height | approx. `56px` |
| Width | fills available space |

**Wishlist (circle) button beside add-to-cart:**
| Property | Value |
|----------|-------|
| Background | `#FFFFFF` |
| Border | `1px solid #C8C9CC` |
| Border-radius | `50%` |
| Size | `56×56px` approx. |

### 10.6 Badges & Stickers

#### Promo Sticker (campaign discount)
```css
background-color: rgb(172, 119, 194);  /* #AC77C2 — purple */
color:            #000000;
font-size:        14px;
font-weight:      400;
padding:          2px 6px;
border-radius:    0px;
text-transform:   uppercase;
```

#### NOWOŚĆ / NEW Sticker
```css
background-color: transparent;
color:            #000000;
font-size:        14px;
font-weight:      400;
/* border or outline used — see transparent-background class */
```

#### Sale Discount Sticker (product tile)
```css
/* Container */
background-color: rgb(217, 46, 49);  /* #D92E31 */
padding:          0 4px;

/* Text */
color:       #FFFFFF;
font-size:   10px;
font-weight: 500;
```

#### Hot Badge (🔥)
```css
background-color: rgb(255, 222, 222);  /* #FFDEDE */
color:            rgb(244, 61, 61);    /* #F43D3D */
font-size:        8px;
font-weight:      500;
border-radius:    999px;
padding:          0 3px;
```

#### Sinsay Club Badge
- Orange/warm accent background (`--colors-primary-50` / `#E09243`)
- Contains club points display: `+N pkt Sinsay Club`
- Font-size: `12px`

### 10.7 Buttons

#### Primary CTA — Dark Pill (Add to Cart)
```css
background-color: #16181D;
color:            #FFFFFF;
border:           none;
border-radius:    999px;
padding:          0 20px;
font-size:        14px;
font-weight:      400;
height:           56px;
transition:       all;

/* Hover state */
background-color: #303133;  /* --colors-dark-80 */
```

#### Primary CTA — Accent (Orange)
```css
background-color: var(--colors-primary-50);  /* #E09243 */
color:            #000000 or #FFFFFF;
border-radius:    999px;
```

#### Secondary Button — Outlined Dark
```css
background-color: transparent;
border:           1px solid #303133;
color:            #303133;
border-radius:    999px;
```

#### Secondary Button — Outlined Light
```css
background-color: transparent;
border:           1px solid rgba(255,255,255,0.8);
color:            #FFFFFF;
border-radius:    999px;
```

#### Filter Pill / Chip (Category Filter)
```css
background-color: transparent;
border:           none;
color:            #000000;
font-size:        14px;
```

### 10.8 Form Inputs

#### Text / Email Input
```css
background-color: #FFFFFF;
border:           1px solid rgb(199, 200, 201);  /* #C7C8C9 */
border-radius:    6px;
padding:          0 16px;
font-size:        14px;
color:            #16181D;
height:           40px–48px (varies by context);

/* Focus */
border-color:     var(--colors-primary-50);  /* #E09243 */
outline:          2px solid var(--colors-primary-50);
outline-offset:   1px;

/* Error */
border-color:     var(--colors-red-50);  /* #FF0023 */
filter:           drop-shadow(0px 0px 8px rgba(255,0,35,0.48));
```

#### Checkbox
- Standard browser checkbox, custom styled
- Active color: `--colors-primary-50`

### 10.9 Category Circles

```
  ┌───┐
  │img│  ← 65×65px, border-radius: 50%, object-fit: fill
  └───┘
  Label   ← 14px, weight 400, color #16181D, text-align: center
```

| Property | Value |
|----------|-------|
| Image size | `~65×65px` |
| Image border-radius | `50%` |
| Label font-size | `14px` |
| Label font-weight | `400` |
| Label color | `#16181D` |
| Label text-align | `center` |
| Label line-height | `18.2px` |
| Container layout | `flex` |

### 10.10 Footer

```
┌─────────────────────────────────────────────────────────────────┐
│  ZAKUPY ONLINE    DOSTAWA I ZWROTY    MARKA SINSAY              │
│  ─────────────   ─────────────────   ───────────                │
│  link            link                link                       │
│  link            link                link                       │
│  ...             ...                 ...                        │
│                                                                 │
│  OBSERWUJ NAS: [FB][IG][TT][PIN][YT]                           │
│  POBIERZ APLIKACJĘ: [Google Play][App Store]                    │
│                                         [Polska (Poland) ▼]    │
├─────────────────────────────────────────────────────────────────┤
│  Polityka prywatności · Cookies · ...                           │
│  LPP S.A., ul. Łąkowa 39/44, 80-769 Gdańsk ...                 │
│                                          © 2026 Sinsay          │
└─────────────────────────────────────────────────────────────────┘
```

| Property | Value |
|----------|-------|
| Background | `#FFFFFF` |
| Text color | `rgb(35,35,35)` / `#232323` |
| Padding-top | `49px` |
| Section heading font-size | `14px` |
| Section heading font-weight | `600` |
| Section heading text-transform | `uppercase` |
| Section heading letter-spacing | `0.2px` |
| Footer link font-size | `16px` |
| Footer link color | `#303133` |
| Footer link text-decoration | `none` |

---

## 11. CSS Custom Properties Reference

Full set of `:root` CSS custom properties extracted from the production site. Use these directly in your project.

### Core Design Tokens

```css
:root {
  /* ── FONT ─────────────────────────────────── */
  --font-family-base: 'Euclid Circular B';
  --font-size-base: 16px;
  --font-size-heading-mega: 80px;
  --font-size-heading-xl: 48px;
  --font-size-heading-l: 32px;
  --font-size-heading-m: 24px;
  --font-size-heading-s: 20px;
  --font-size-default-xl: 18px;
  --font-size-default-l: 16px;
  --font-size-default-m: 14px;
  --font-size-default-s: 12px;
  --font-size-default-xs: 10px;
  --font-weight-bold: 700;
  --font-weight-semibold: 600;
  --font-weight-medium: 500;
  --font-weight-regular: 400;
  --font-h-space-xl: 1px;
  --font-h-space-l: 0.8px;
  --font-h-space-m: 0.6px;
  --font-h-space-sm: 0.3px;
  --font-h-space-s: 0.2px;
  --font-h-space-xs: 0.1px;
  --font-h-space-none: 0;
  --text-case-upper: uppercase;
  --text-case-default: none;
  --text-decor-link: underline;
  --text-decor-discount: line-through;

  /* ── PRIMARY COLOUR ───────────────────────── */
  --color-primary-90: #1A0D00;
  --color-primary-80: #4D2600;
  --color-primary-70: #804306;
  --color-primary-60: #B2671B;
  --color-primary-50: #E09243;
  --color-primary-40: #F2B06D;
  --color-primary-30: #F2C291;
  --color-primary-20: #FFDFBF;
  --color-primary-10: #FFF2E5;
  --color-primary: var(--color-primary-50);

  /* ── DARK SCALE ───────────────────────────── */
  --color-dark-100: #000000;
  --color-dark-90:  #18191a;
  --color-dark-80:  #303133;
  --color-dark-70:  #494a4d;
  --color-dark-60:  #616366;
  --color-dark-50:  #7b7d80;
  --color-dark-40:  #949699;
  --color-dark-30:  #afb0b2;
  --color-dark-20:  #c8c9cc;
  --color-dark-10:  #e3e4e5;
  --color-dark-5:   #f1f2f4;
  --color-white-100: #ffffff;

  /* ── RED / GREEN / BLUE / ORANGE ──────────── */
  --color-red-50:    #ff0023;
  --color-red-60:    #cc001c;
  --color-green-50:  #0db209;
  --color-green-60:  #0a8c07;
  --color-blue-50:   #2e90e5;
  --color-blue-60:   #1268b2;
  --color-orange-50: #ff9900;
  --color-orange-60: #cc7a00;

  /* ── ALPHA OVERLAYS ───────────────────────── */
  --color-dark-alpha-90: rgba(0,0,0,0.9);
  --color-dark-alpha-80: rgba(0,0,0,0.8);
  --color-dark-alpha-70: rgba(0,0,0,0.7);
  --color-dark-alpha-60: rgba(0,0,0,0.6);
  --color-dark-alpha-50: rgba(0,0,0,0.5);
  --color-dark-alpha-40: rgba(0,0,0,0.4);
  --color-dark-alpha-30: rgba(0,0,0,0.3);
  --color-dark-alpha-20: rgba(0,0,0,0.2);
  --color-dark-alpha-10: rgba(0,0,0,0.1);
  --color-dark-alpha-5:  rgba(0,0,0,0.05);
  --color-white-alpha-90: rgba(255,255,255,0.9);
  --color-white-alpha-50: rgba(255,255,255,0.5);
  --color-white-alpha-20: rgba(255,255,255,0.2);
  --color-white-alpha-10: rgba(255,255,255,0.1);

  /* ── SPACING ──────────────────────────────── */
  --size-s:   4px;
  --size-sm:  8px;
  --size-m:   16px;
  --size-l:   32px;
  --size-xl:  64px;
  --size-xxl: 128px;
  --spacing-other-space-2:  2px;
  --spacing-other-space-12: 12px;
  --spacing-other-space-24: 24px;
  --spacing-other-space-40: 40px;
  --spacing-other-space-48: 48px;
  --spacing-other-space-56: 56px;
  --spacing-other-space-72: 72px;
  --spacing-other-space-80: 80px;
  --spacing-other-space-88: 88px;

  /* ── VIEWPORT ─────────────────────────────── */
  --viewport-xs: 320px;
  --viewport-s:  640px;
  --viewport-m:  1008px;
  --viewport-l:  1540px;

  /* ── BORDER RADIUS ────────────────────────── */
  --radius-base-xs:  2px;
  --radius-base-s:   4px;
  --radius-base-sm:  8px;
  --radius-base-m:   16px;
  --radius-base-l:   32px;
  --radius-pill-xl:  32px;
  --radius-full:     999px;

  /* ── ELEVATION / SHADOW ───────────────────── */
  --elevation-01: 0px 2px 12px rgba(24,25,26,0.08), 0px 1px 2px rgba(26,13,0,0.08);
  --elevation-02: 0px 4px 16px rgba(24,25,26,0.10), 0px 1px 4px rgba(26,13,0,0.10);
  --elevation-03: 0px 6px 24px rgba(24,25,26,0.12), 0px 1px 6px rgba(26,13,0,0.10);
  --elevation-04: 0px 8px 40px rgba(24,25,26,0.14), 0px 2px 12px rgba(26,13,0,0.12);

  /* ── LAYOUT ───────────────────────────────── */
  --layout-wrapper-base-base:   1440px;
  --layout-wrapper-base-wide:   1500px;
  --layout-wrapper-base-narrow: 1140px;
  --layout-wrapper-base-fluid:  100%;
  --layout-breakpoint-desktop-large: 1540px;
  --layout-breakpoint-desktop-small: 1008px;
  --layout-breakpoint-tablet:        640px;
  --layout-breakpoint-mobile:        0px;
  --layout-gutter-base-desktop: 24px;
  --layout-gutter-base-tablet:  24px;
  --layout-gutter-base-mobile:  16px;
  --layout-gutter-special-products-view: 4px;
  --layout-margin-base-desktop-large: 64px;
  --layout-margin-base-desktop-small: 32px;
  --layout-margin-base-tablet:  24px;
  --layout-margin-base-mobile:  16px;
}
```

---

## Screenshots Reference

All screenshots captured during this audit are stored in `docs/assets/`:

| File | Description |
|------|-------------|
| `docs/assets/sinsay-homepage.png` | Homepage — desktop viewport (960px), above fold |
| `docs/assets/sinsay-homepage-full.png` | Homepage — desktop, full page scroll |
| `docs/assets/sinsay-product-listing.png` | Women's clothing listing page, above fold |
| `docs/assets/sinsay-product-grid.png` | Product grid — desktop, visible tiles |
| `docs/assets/sinsay-product-detail.png` | Product detail — above fold (product image) |
| `docs/assets/sinsay-product-detail-info.png` | Product detail — info panel (price, badges, sizes) |
| `docs/assets/sinsay-mobile-homepage.png` | Homepage — mobile viewport (390×844px) |

---

*Generated by automated Playwright design audit — SilkyCoders project.*
