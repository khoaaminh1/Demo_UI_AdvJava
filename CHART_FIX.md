# 🔧 Hướng Dẫn Khắc Phục Biểu Đồ Dashboard

## ❌ Vấn Đề

Biểu đồ trong trang **dashboard.html** (từ controller) không hiển thị, nhưng biểu đồ trong **chart-test.html** (static file) hoạt động tốt.

## ✅ Các Thay Đổi Đã Thực Hiện

### 1. **Cải thiện Canvas Container** (dashboard.html)

**Trước:**
```html
<canvas id="catChart" height="240"></canvas>
```

**Sau:**
```html
<div style="position: relative; height: 300px;">
  <canvas id="catChart"></canvas>
</div>
```

**Lý do:** 
- Chart.js hoạt động tốt hơn với responsive container
- Tránh conflict giữa fixed height và responsive mode

---

### 2. **DOMContentLoaded Wrapper** (dashboard.html)

**Trước:**
```javascript
<script th:inline="javascript">
  const catLabels = [[${vm.categoryLabels}]];
  renderDoughnut('catChart', catLabels, catValues);
</script>
```

**Sau:**
```javascript
<script th:inline="javascript">
  document.addEventListener('DOMContentLoaded', function() {
    const catLabels = /*[[${vm.categoryLabels}]]*/ [];
    // ... conversion logic
    renderDoughnut('catChart', catLabels, catValuesNum);
  });
</script>
```

**Lý do:**
- Đảm bảo canvas elements đã được render trước khi Chart.js tạo charts
- Tránh race condition giữa Thymeleaf rendering và JavaScript execution

---

### 3. **BigDecimal to Number Conversion**

**Thêm logic chuyển đổi:**
```javascript
const catValuesNum = catValues.map(v => {
  if (typeof v === 'object' && v !== null) {
    return parseFloat(v.toString());
  }
  return parseFloat(v);
});
```

**Lý do:**
- Java BigDecimal được serialize thành object trong Thymeleaf
- Chart.js cần JavaScript numbers thuần túy

---

### 4. **Enhanced Debug Logging**

**Thêm console logs:**
```javascript
console.log('=== Dashboard Chart Data ===');
console.log('Category Labels:', catLabels);
console.log('Category Values:', catValues);
console.log('Converted Values:', catValuesNum);
```

**Lý do:**
- Dễ dàng debug khi có vấn đề
- Xem được data flow từ backend → frontend

---

### 5. **App.js Initialization Fix**

**Trước:**
```javascript
document.addEventListener('DOMContentLoaded', function() {
  updateBudgetProgress();
  // ... form validation
});
```

**Sau:**
```javascript
if (document.readyState === 'loading') {
  document.addEventListener('DOMContentLoaded', initializeApp);
} else {
  initializeApp();
}

function initializeApp() {
  updateBudgetProgress();
  // ... form validation
}
```

**Lý do:**
- Tránh multiple DOMContentLoaded listeners conflict
- Xử lý trường hợp script load sau khi DOM ready

---

## 🧪 Testing Pages

### 1. **chart-test.html** - Biểu đồ test cơ bản
```
http://localhost:8080/chart-test.html
```
- Static HTML với test data
- Không có Thymeleaf
- Dùng để test Chart.js functions

### 2. **dashboard-debug.html** - Giống dashboard thật
```
http://localhost:8080/dashboard-debug.html
```
- Có layout giống dashboard
- Static test data
- Debug information panel
- **Nếu charts hoạt động ở đây → structure đúng**

### 3. **Dashboard thật** - Từ controller
```
http://localhost:8080/dashboard
```
- Có Thymeleaf
- Real data từ FakeDataService
- Cần backend chạy

---

## 🔍 Debug Steps

### 1. Kiểm tra Console (F12)

Mở Developer Tools và xem Console. Bạn sẽ thấy:

```
=== Dashboard Chart Data ===
Category Labels: ['Food & Drinks', 'Transport', ...]
Category Values: [250.5, 120.75, ...]
Converted Values: [250.5, 120.75, ...]
Rendering doughnut chart...
Rendering line chart...
=== Chart rendering complete ===
```

**Nếu không thấy logs:**
- Script không chạy
- Check xem script tag có trong HTML không

**Nếu thấy errors:**
- Check error message
- Thường là: Canvas not found, Data format wrong, Chart.js not loaded

---

### 2. Kiểm tra Canvas Elements

Trong Console, type:
```javascript
document.getElementById('catChart')
document.getElementById('flowChart')
```

**Nên return:**
```
<canvas id="catChart"></canvas>
<canvas id="flowChart"></canvas>
```

**Nếu return null:**
- Canvas chưa được render
- Check Thymeleaf syntax
- Check if card is hidden

---

### 3. Kiểm tra Chart.js

Trong Console, type:
```javascript
Chart
renderDoughnut
renderLine
```

**Nên return:**
```
function Chart() { ... }
function renderDoughnut() { ... }
function renderLine() { ... }
```

**Nếu undefined:**
- Chart.js chưa load
- app.js chưa load
- Check CDN link

---

### 4. Kiểm tra Data từ Backend

Trong Console, type:
```javascript
catLabels
catValues
months
```

**Nên return arrays với data:**
```javascript
['Food & Drinks', 'Transport', ...]
[250.5, 120.75, ...]
['May 24', 'Jun 24', ...]
```

**Nếu undefined hoặc empty:**
- Backend không trả data
- Check DashboardController
- Check FakeDataService.buildDashboard()

---

## 🎯 Checklist

Sau khi reload trang dashboard, check:

- [ ] Console logs xuất hiện
- [ ] Không có JavaScript errors
- [ ] Canvas elements tồn tại trong DOM
- [ ] Data arrays có values
- [ ] Doughnut chart hiển thị
- [ ] Line chart hiển thị
- [ ] Charts interactive (hover tooltips)
- [ ] Charts responsive (resize browser)

---

## 💡 Common Issues & Solutions

### Issue 1: "Cannot read property 'getContext' of null"
**Solution:** Canvas element chưa tồn tại. Wrap code trong DOMContentLoaded.

### Issue 2: "Chart is not defined"
**Solution:** Chart.js chưa load. Check CDN link trong head.html.

### Issue 3: Charts hiển thị nhưng không có data
**Solution:** Backend không trả data. Check controller và service.

### Issue 4: Charts quá nhỏ hoặc không responsive
**Solution:** Thêm wrapper div với height, remove fixed height từ canvas.

### Issue 5: "parseFloat returns NaN"
**Solution:** Data format sai. Log data để xem structure và fix conversion logic.

### Issue 6: Charts chỉ hiển thị lần đầu, reload không thấy
**Solution:** Chart instance chưa được destroy. Thêm Chart.getChart() check.

---

## 🔄 Reload & Test Workflow

1. **Save changes** trong VS Code
2. **Stop Spring Boot** (Ctrl+C trong terminal)
3. **Start lại**: `./mvnw spring-boot:run`
4. **Clear browser cache**: Ctrl+Shift+R
5. **Open dashboard**: http://localhost:8080/dashboard
6. **Open DevTools**: F12
7. **Check Console**: Xem logs và errors
8. **Check Network**: Xem app.js và Chart.js loaded chưa

---

## 📝 Files Modified

1. ✅ **dashboard.html**
   - Canvas wrappers
   - DOMContentLoaded
   - Data conversion
   - Debug logs

2. ✅ **app.js**
   - initializeApp function
   - DOMContentLoaded handler fix

3. ✅ **dashboard-debug.html** (NEW)
   - Debug page
   - Same structure as dashboard

4. ✅ **CHART_FIX.md** (THIS FILE)
   - Documentation
   - Troubleshooting guide

---

## ✨ Next Steps

1. **Test dashboard-debug.html first**
   - Nếu charts work → structure đúng
   - Nếu không work → fix app.js

2. **Test real dashboard**
   - Nếu charts work → SUCCESS! 🎉
   - Nếu không work → check backend data

3. **If still not working:**
   - Share console errors
   - Share network tab screenshots
   - Share backend logs

---

**Good luck! 🚀**
