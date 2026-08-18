---
name: "feat(finance): support edit & delete of finance records (UI + ViewModel)"
about: "Adds editing and deletion flows for finance records, including UI dialogs and ViewModel/DAO support."
title: "feat(finance): support edit & delete of finance records (UI + ViewModel)"
labels:
  - feature
  - ui
---

Summary
-------

This PR adds UI and data-layer support to edit and delete finance records.

What changed
------------

- UI
  - AddFinanceRecordDialog: now supports editing an existing FinanceRecord (prefills fields), uses a dropdown category selector and toggles between Save / Update.
  - ConfirmDeleteDialog: new reusable confirmation dialog for destructive actions.
  - FinanceScreen: per-transaction Edit and Delete actions are shown; exposes onEditTransaction/onDeleteTransaction callbacks.
  - MkulimaApp: wiring for opening the edit dialog prefilled and showing the delete confirmation. Calls ViewModel.updateFinanceRecord and ViewModel.deleteFinanceRecord accordingly.

- Data / ViewModel
  - FarmDao: update/delete methods for finance records (updateFinanceRecord, deleteFinanceRecordById).
  - FarmRepository: wrappers for update/delete (already present/updated earlier).
  - FarmViewModel: added updateFinanceRecord(...) and deleteFinanceRecord(...) methods.

Manual test checklist
---------------------

- [ ] Add transaction -> appears in list and totals update
- [ ] Edit transaction -> dialog opens prefilled; update persists and totals update
- [ ] Delete transaction -> confirmation appears; deletion removes record and totals update

Notes
-----

- Category wording and button text follow the existing app style; change requests welcome.
- This PR only touches UI wiring and local DB operations — no network migrations required.
