# 🎯 Assessment Setter/Checker Management - Quick Guide

## What's New?

The Assessment Detail Page now includes comprehensive role management for setters and checkers!

---

## 📸 UI Overview

### Main Features:

```
┌─────────────────────────────────────────────────────────────┐
│ Assessment Detail Page                                       │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  📋 Setters & Checkers                    [Assign Role]     │
│  ─────────────────────────────────────────────────────────  │
│                                                              │
│  🔵 SETTERS                    🟢 CHECKERS                  │
│  ├─ Dr. Jane Smith            ├─ Prof. John Doe             │
│  │  jane@sheffield.ac.uk      │  john@sheffield.ac.uk       │
│  │  [Remove]                  │  🏅 Module Moderator         │
│  │                            │  [Remove]                    │
│  │                            │                              │
│  └─ 📝 Responsibilities:       └─ ✅ Responsibilities:        │
│     • Create assessment            • Review for quality      │
│     • Submit for checking          • Provide feedback        │
│     • Address feedback             • Approve or reject       │
│     • Make revisions               • Must be independent     │
│                                                              │
│  ⚠️  Important: Checkers must be independent (not module    │
│      lead, not module staff, not setter)                    │
│                                                              │
│  💬  Feedback Guidelines: Use non-content feedback like     │
│      "Question 2a unclear" not content-revealing details    │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎬 Quick Actions

### 1. Assign a Setter (Module Lead or Admin)

**Steps:**
1. Click **"Assign Role"** button
2. Select **"Setter"** from dropdown
3. Choose a **module staff member**
4. Click **"Assign Role"**

**Who can be setter:**
- ✅ Module Lead (usually)
- ✅ Module Staff members
- ✅ Academic users on the module
- ❌ External Examiners
- ❌ Non-module staff

---

### 2. Assign a Checker (Module Lead or Admin)

**Steps:**
1. Click **"Assign Role"** button
2. Select **"Checker"** from dropdown
3. Choose an **independent academic**
4. Click **"Assign Role"**

**Who can be checker:**
- ✅ Module Moderator (auto-assigned)
- ✅ Academic users NOT on module
- ✅ Independent academics
- ❌ Module Lead
- ❌ Module Staff
- ❌ Setters on this assessment
- ❌ External Examiners

---

### 3. Remove a Role

**Steps:**
1. Find the user in Setters or Checkers section
2. Click **"Remove"** button
3. Confirm removal

---

## 🔄 Typical Workflow

```
DAY 1: Setup
─────────────────
Module Lead creates assessment
  ↓
System auto-assigns Moderator as CHECKER ✅
  ↓
Module Lead assigns SETTER (often themselves)
  ↓
Ready to start!


DAY 2-7: Development
─────────────────────
SETTER creates assessment questions (DRAFT)
  ↓
SETTER submits for checking (→ READY_FOR_CHECK)
  ↓
CHECKER reviews assessment


DAY 8: Review Cycle
────────────────────
CHECKER provides feedback
  ├─ Approves → RELEASED ✅
  └─ Requests changes → CHANGES_REQUIRED
      ↓
      SETTER makes changes
      ↓
      SETTER resubmits → READY_FOR_CHECK
      ↓
      [Cycle repeats until approved]


DAY 10+: Final
──────────────
CHECKER approves → RELEASED
Assessment published to students ✅
```

---

## 💡 Smart Features

### 🎯 Automatic Filtering
The system automatically shows only eligible users:
- **Setters dropdown:** Only module staff
- **Checkers dropdown:** Only independent academics
- If empty: Helpful message explains why

### 🔒 Permission Checks
- Only Module Leads and Admins can manage roles
- Validation happens on both frontend and backend
- Clear error messages if something goes wrong

### 📊 Visual Indicators
- **Blue** = Setters
- **Green** = Checkers
- **Yellow** = Warnings/Important info
- **Purple** = Feedback guidelines
- **🏅 Badge** = Module Moderator

### 🔄 Real-time Updates
- Role changes appear immediately
- No page refresh needed
- Automatic data synchronization

---

## ⚠️ Important Rules (from Q&A)

### Q3: Can module lead be setter/checker?
**Answer:** 
- ✅ Module lead is **usually a setter**
- ❌ Module lead **cannot be checker** (conflict of interest)
- Must use independent academic for checking

### Q9: How many revisions allowed?
**Answer:**
- 🔄 **Unlimited** for tests and coursework
- Setter can resubmit as many times as needed
- Checker can request changes multiple times
- Each iteration tracked in history

### Q10: What feedback is allowed?
**Answer:**
- ✅ **Non-content feedback:** "Question 2a unclear"
- ✅ **Structural feedback:** "Weightings sum to 80 not 100"
- ❌ **Content feedback:** Upload to secure storage instead
- 💬 Reference: "See detailed feedback in secure storage"

### Q11: What data is stored?
**Answer:**
- ✅ **Process tracking:** Who did what, when
- ✅ **State transitions:** Full workflow history
- ✅ **Non-content feedback:** Safe comments
- ❌ **Assessment content:** Stored separately
- ❌ **Question details:** Not in this system

---

## 🎓 Best Practices

### For Module Leads:
1. ✅ Assign yourself or a staff member as SETTER
2. ✅ Let the auto-assigned MODERATOR be CHECKER (best practice)
3. ✅ Only change CHECKER if there's a conflict
4. ✅ Multiple SETTERS for team assessments is fine

### For Setters:
1. ✅ Create thorough assessments before submitting
2. ✅ Address all checker feedback completely
3. ✅ Use the feedback loop until approved
4. ✅ Respond professionally to external examiners (exams)

### For Checkers:
1. ✅ Provide clear, actionable feedback
2. ✅ Don't reveal question content in comments
3. ✅ Be thorough but constructive
4. ✅ Use the CHANGES_REQUIRED state freely

---

## 🐛 Common Issues

### "No eligible users" when assigning checker
**Reason:** All academics are module staff
**Solution:** 
- Use the module moderator (already assigned)
- Request an external academic be added to system

### "User already has this role"
**Reason:** Duplicate assignment attempt
**Solution:** 
- Check if user is already listed
- Remove first if you want to reassign

### Can't assign specific user as checker
**Reason:** User doesn't meet independence criteria
**Solution:**
- Choose different academic
- Check if user is module lead/staff
- Verify user is not already a setter

### Role assignment button missing
**Reason:** You're not a module lead or admin
**Solution:**
- Contact module lead to assign roles
- Or contact system admin for access

---

## 📱 Mobile/Responsive Design

The UI adapts to all screen sizes:
- **Desktop:** Side-by-side setters and checkers
- **Tablet:** Stacked layout with full width
- **Mobile:** Single column, touch-friendly buttons

---

## 🔗 Related Pages

- **Dashboard:** See all your assessments
- **Module Detail:** View all module assessments
- **Admin Page:** Manage users and modules
- **Assessment Creation:** Start new assessments

---

## 📞 Need Help?

### Quick Reference:
- **Backend Guide:** `docs/HOW_TO_ADD_SETTER_CHECKER.md`
- **Frontend Guide:** `docs/FRONTEND_SETTER_CHECKER.md`
- **Workflow Guide:** `docs/ASSESSMENT_WORKFLOWS.md`
- **API Documentation:** See backend controller comments

### Support:
- Contact Teaching Support for user/role issues
- Contact Module Lead for assessment-specific questions
- Check transition history for workflow problems

---

**Quick Tips:**
- 💡 Hover over badges for more info
- 🔄 Page auto-refreshes after role changes
- ⌨️ Use Tab key to navigate modal forms
- 🖱️ Click outside modal to close

---

**Version:** 1.0  
**Last Updated:** 2025-11-10  
**Status:** ✅ Production Ready
