# Domain Dictionary

## Core Concepts

* **Return (Zwrot):** Voluntary, 30-day strict limit, item must be unused with tags. Requires receipt verification via OCR.

* **Complaint (Reklamacja):** Statutory warranty under Polish Civil Code, 2-year limit, covers manufacturing defects (seam slippage, pilling, color bleeding, etc.). Requires defect photo analysis.

* **Receipt Verification:** Mandatory for returns. AI extracts order/receipt ID and purchase date, verifies authenticity, matches user-provided data.

* **Defect Analysis:** For complaints. AI classifies defect type and determines if it's a valid manufacturing defect (eligible) or user-caused damage (not eligible).

* **Conversation ID:** Unique identifier generated for each valid request, used to maintain transient conversation state during active session.
