# 

## Original Prompt

Use playwright plugin to visit sinsay.com eCommenrce website and make detailed analyses of Design Tokens, all
specific colors, fonts, margins, paddings, borders, rounded, etc. to create detailed Style Guides based on this
website, that will be later used to create new application for Sinsay that will match design system of Sinsay
website. Take screenshots and analyse them too, but also analyse DOM and styles. Prepare this document in our /docs
folder and make it detailed, provide exact intructions for FE Agent to help re-created this exact style and design.

## Prompt Cowboy refined v1



## Prompt Cowboy refined v2 (from A.S.)

### Situation
You are working on a project to create a new application for Sinsay that needs to match their existing website's design system. To ensure visual consistency, you need to extract and document all design tokens and styling specifications from the current sinsay.com website.

### Task
- The assistant should use the Playwright plugin to visit sinsay.com and conduct a comprehensive design system analysis. The assistant should:
- Navigate to sinsay.com and capture screenshots of key pages and components
- Inspect the DOM structure and extract CSS styles programmatically
- Document all design tokens including colors (with hex/RGB values), typography (font families, sizes, weights, line heights), spacing (margins, paddings), borders (widths, styles, colors), border radius values, shadows, and transitions
- Organize findings into a detailed style guide document
- Save the complete documentation in the /docs folder
- Structure the guide to provide actionable specifications that a frontend developer can use to recreate the design system accurately

### Objective
Create a comprehensive, production-ready style guide that captures Sinsay's complete design system, enabling frontend developers to build a new application that achieves pixel-perfect visual consistency with the original website.

### Knowledge
- The analysis should cover both visual inspection (screenshots) and technical inspection (DOM/CSS analysis)
- Design tokens are the atomic values of a design system (colors, typography, spacing, etc.)
- The documentation should be detailed enough that a frontend developer can implement the design without needing to reference the original website
- Include component-level patterns where identifiable (buttons, cards, navigation, forms, etc.)
- Document responsive breakpoints if present
Note any CSS variables or design system conventions already in use on the site
