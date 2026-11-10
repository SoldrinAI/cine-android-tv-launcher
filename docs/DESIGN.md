---
name: Cinematic Luxury TV OS
colors:
  surface: '#121316'
  surface-dim: '#121316'
  surface-bright: '#38393c'
  surface-container-lowest: '#0d0e11'
  surface-container-low: '#1b1b1f'
  surface-container: '#1f1f23'
  surface-container-high: '#292a2d'
  surface-container-highest: '#343538'
  on-surface: '#e3e2e6'
  on-surface-variant: '#d0c5af'
  inverse-surface: '#e3e2e6'
  inverse-on-surface: '#2f3034'
  outline: '#99907c'
  outline-variant: '#4d4635'
  surface-tint: '#e9c349'
  primary: '#f2ca50'
  on-primary: '#3c2f00'
  primary-container: '#d4af37'
  on-primary-container: '#554300'
  inverse-primary: '#735c00'
  secondary: '#e1c290'
  on-secondary: '#402d08'
  secondary-container: '#58431c'
  on-secondary-container: '#cfb180'
  tertiary: '#cdced6'
  on-tertiary: '#2e3036'
  tertiary-container: '#b1b2ba'
  on-tertiary-container: '#43454b'
  error: '#ffb4ab'
  on-error: '#690005'
  error-container: '#93000a'
  on-error-container: '#ffdad6'
  primary-fixed: '#ffe088'
  primary-fixed-dim: '#e9c349'
  on-primary-fixed: '#241a00'
  on-primary-fixed-variant: '#574500'
  secondary-fixed: '#fedeaa'
  secondary-fixed-dim: '#e1c290'
  on-secondary-fixed: '#271900'
  on-secondary-fixed-variant: '#58431c'
  tertiary-fixed: '#e2e2ea'
  tertiary-fixed-dim: '#c5c6ce'
  on-tertiary-fixed: '#191b21'
  on-tertiary-fixed-variant: '#45464d'
  background: '#121316'
  on-background: '#e3e2e6'
  surface-variant: '#343538'
typography:
  display-hero:
    fontFamily: Bodoni Moda
    fontSize: 56px
    fontWeight: '600'
    lineHeight: 64px
    letterSpacing: -0.02em
  headline-xl:
    fontFamily: Bodoni Moda
    fontSize: 40px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.01em
  headline-lg:
    fontFamily: Bodoni Moda
    fontSize: 28px
    fontWeight: '500'
    lineHeight: 36px
    letterSpacing: 0em
  title-shelf:
    fontFamily: Manrope
    fontSize: 22px
    fontWeight: '600'
    lineHeight: 28px
    letterSpacing: 0.01em
  title-card:
    fontFamily: Manrope
    fontSize: 16px
    fontWeight: '600'
    lineHeight: 22px
    letterSpacing: 0em
  body-synopsis:
    fontFamily: Manrope
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 24px
    letterSpacing: 0.01em
  label-caps:
    fontFamily: Manrope
    fontSize: 12px
    fontWeight: '700'
    lineHeight: 16px
    letterSpacing: 0.14em
  label-badge:
    fontFamily: Manrope
    fontSize: 11px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
  clock-masthead:
    fontFamily: Manrope
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 18px
    letterSpacing: 0.08em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1.5rem
  margin: 3.5rem
  space-xs: 0.5rem
  space-sm: 0.75rem
  space-md: 1.25rem
  space-lg: 2rem
  space-xl: 3rem
---

## Brand & Style

This design system delivers a premier 10-foot television operating system experience crafted for large displays viewed from across the room. It merges the cinematic grandeur of an Apple TV presentation, the immediate discovery ergonomics of Netflix, the meticulous acoustic-industrial luxury of Bang & Olufsen, and the disciplined visual precision of Sony master displays.

The aesthetic philosophy centers on restrained luxury and absolute editorial clarity. Deep obsidian and architectural charcoal planes recede into the room's physical darkness, allowing 4K/HDR cinematography to ignite the foreground. Accents of champagne gold punctuate active D-pad focus states with jewelry-grade restraint. Typography pairs high-contrast editorial serifs for cinematic mastheads with ultra-legible, geometric sans-serif structural elements engineered for glanceability from 10 feet. Visual weight communicates through razor-sharp planes, subtle luminous borders, and micro-scaled surface focus reactions rather than heavy synthetic shadows or aggressive skeuomorphism.

## Colors

The palette operates under an uncompromising dark-mode architecture engineered specifically for OLED and precision-backlit television panels:

- **Deep Cinematic Obsidian (`#101114`)**: Serves as the canvas substrate, merging with bezel edges in low-light home theater environments to eliminate visual boundaries.
- **Dark Charcoal Surface (`#191A1F`)**: Defines primary navigation rails, background trays, and non-focused container planes.
- **Tactile Elevated Surface (`#22242A`)**: Forms the structural body for media shelf cards, setting panels, and interactive tiles.
- **Champagne Gold Seed & Focus (`#D4AF37` / `#C5A878`)**: Applied with utmost restraint. Reserved exclusively for remote D-pad focus halos, active tab pips, prestige curation badges (4K HDR, Dolby Atmos, IMAX Enhanced), and critical status indications.
- **Primary Text (`#F4F3F0`)**: A warm, optical off-white designed to counteract halation and light bleed on high-nit displays while guaranteeing effortless 10-foot legibility.
- **Muted Metadata Slate (`#92949B`)**: Dedicated to runtimes, release years, age certifications, and secondary narrative synopses.

## Typography

Typography bridges editorial grandeur and functional tele-screen distance optics.

- **Editorial Serifs (`Bodoni Moda`)**: Assigned to flagship hero titles, featured curation banners, and marquee numbers. It injects high-contrast dramatic tension that elevates the OS from an ordinary utility grid to an architectural art publication.
- **Precision Sans-Serif (`Manrope`)**: Provides structural geometry, tall x-height, and open apertures essential for quick scanning at a 10-foot viewing distance.
- **Casing & Tracking Rules**:
  - All category labels, technical badges (e.g., `DOLBY VISION`, `4K MASTER`), and utility chips must be styled in uppercase with tracking spanning `0.10em` to `0.16em`.
  - Synopsis text must be clamped to 3 lines maximum on shelf headers to prevent visual fatigue, rendered in `#92949B` with generous line-height (`1.6`) for rapid ambient comprehension.

## Layout & Spacing

Engineered precisely for fixed 16:9 television viewport standards (native 1920x1080 baseline canvas):

- **Overscan & Safe Margins**: Standardized outer margin of `3.5rem` (56px) establishes an unbreachable TV title-safe perimeter on all 4 borders, safeguarding UI elements from edge-panel clipping and bezel shadowing.
- **Horizontal Carousels**: Content shelves use a consistent `1.5rem` (24px) column gutter. Carousels overflow past the right safe boundary to indicate continuity, while the active card left-aligns with the active focus anchor.
- **Vertical Hierarchy**:
  - Top Navigation Bar: Height fixed at `4.5rem` (72px).
  - Hero Spotlight Arena: Occupies the top 48% of the viewport; title and metadata anchor strictly in the lower quadrant against a localized non-linear obsidian gradient.
  - Media Rows: Shift dynamically upward by 240px upon downward D-pad navigation to bring browsing shelves into comfortable eye-level alignment.

## Elevation & Depth

Depth is conveyed through luminance stratification, focus scaling, and optical highlight borders rather than traditional downward drop-shadows:

- **Level 0 (Base Substrate)**: Canvas background `#101114`.
- **Level 1 (Resting Shelf Cards)**: Flat surfaces at `#191A1F` or `#22242A` framed by a subtle 1px border of `rgba(244, 243, 240, 0.08)`. No blur, no drop shadow.
- **Level 2 (D-pad Focus State)**:
  - Scale transform: `1.04x` smoothly interpolated via standard TV bezier timing (`cubic-bezier(0.2, 0.0, 0.0, 1.0)` over 220ms).
  - Rim highlight: 2px solid `#C5A878`.
  - Ambient aura: `0 8px 24px rgba(212, 175, 55, 0.22), 0 2px 6px rgba(0, 0, 0, 0.6)`.
- **Atmospheric Scrims**: The hero background features a localized directional radial-gradient emanating from `#101114` at the bottom-left corner (`stop 0% at 90% opacity, stop 70% at 0% opacity`), leaving the right quadrant crystal-clear for visual photography.

## Shapes

The design system employs a soft yet disciplined edge radius (Level 1: 4px base, 8px on larger media cards). This reflects Bang & Olufsen's milled-aluminum architecture: sharp enough to project structural luxury and high technical competence, yet softly radiused to avoid optical harshness on pixel grids.

- Standard media cards (16:9 and 2:3): `border-radius: 0.5rem` (8px).
- App launcher icons and technical metadata badges: `border-radius: 0.25rem` (4px).
- D-pad focus outlines conform strictly to the card's native border-radius with an external 2px offset padding to maintain razor-sharp perimeter fidelity.

## Components

### 1. D-Pad Interactive States (Global TV Remote Pattern)
Every interactive element must cycle through 4 distinct remote control states:
- **Default**: Card surface `#22242A`, 1px border `rgba(244, 243, 240, 0.08)`, 1.0x scale.
- **Focused**: Scaled `1.04x`, 2px solid `#C5A878` border ring, ambient champagne glow, typography brightens from `#92949B` to `#F4F3F0`.
- **Pressed (Remote Enter)**: Scale dips momentarily to `1.01x`, border brightness flares to `#FFFFFF` for 100ms.
- **Disabled**: Opacity reduced to `0.35`, focus skips this element in D-pad navigation order.

### 2. Media Shelf Cards
- **Landscape (16:9)**: Primary format for "Continue Watching" and episodic content. Displays preview art, bottom progress bar (`2px` height, track `rgba(255,255,255,0.2)`, fill `#C5A878`), title below art in `title-card`, and remaining duration in `label-badge`.
- **Portrait (2:3)**: Standard movie and series poster format for curated editorial shelves. Focus triggers metadata expansion showing release year, rating badge, and genres directly below the card.

### 3. Top Navigation Masthead
- **Brand Mark**: `CINÉ` set in Bodoni Moda uppercase, gold-tinted `#D4AF37`, tracked out to `0.2em`.
- **Navigation Tabs**: Categories ("Search", "Home", "Cinema", "Series", "Live TV", "Apps"). Inactive tabs set in `Manrope` 14px `#92949B`. Active tab rendered in `#F4F3F0` underscored by a centered 4px circular champagne pip.
- **System Cluster**: Right-aligned live 24-hour clock and network/audio mode indicators set in `clock-masthead`.

### 4. Interactive App Launcher Chips
- Squircle tiles (112px x 72px) showcasing streaming services (Netflix, Prime Video, Disney+, Sony LIV, YouTube, Spotify).
- Focused state expands an inline panoramic live preview backdrop above the shelf, accompanied by an audio-haptic subtle sound cue.

### 5. Remote Action Buttons & Meta Badges
- **Hero CTA**: Primary button is filled champagne `#D4AF37` with `#101114` bold typography for immediate call-to-action ("Resume", "Watch Now"). Secondary actions use translucent `#191A1F` with a 1px border.
- **Specification Badges**: Monochromatic pills (`4K`, `HDR10+`, `DOLBY ATMOS`, `IMAX`) with `0.25rem` radius, 1px border `rgba(244,243,240,0.15)`, text in `label-badge`.