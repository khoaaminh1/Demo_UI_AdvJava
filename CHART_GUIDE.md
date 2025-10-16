# 📊 Hướng Dẫn Biểu Đồ Dashboard

## Tổng Quan

Dashboard của Personal Finance Tracker sử dụng **Chart.js** để hiển thị 2 loại biểu đồ chính:
1. **Doughnut Chart** - Spending by Category (Chi tiêu theo danh mục)
2. **Line Chart** - Cash Flow Trend (Xu hướng dòng tiền)

---

## 🍩 1. Spending by Category (Doughnut Chart)

### Mục đích
Hiển thị tỷ lệ phần trăm chi tiêu của từng danh mục trong tháng hiện tại.

### Dữ liệu đầu vào
```javascript
categoryLabels: ['Food & Drinks', 'Transport', 'Shopping', ...]
categoryValues: [250.50, 120.75, 180.25, ...]  // Số tiền chi tiêu
```

### Tính năng
- ✅ **Màu sắc đa dạng**: 10 màu khác nhau cho các danh mục
- ✅ **Tooltip chi tiết**: Hiển thị số tiền và phần trăm khi hover
- ✅ **Legend phía dưới**: Dễ đọc, với icon tròn
- ✅ **Hole 65%**: Tạo hiệu ứng doughnut (vòng tròn rỗng giữa)
- ✅ **Hover effect**: Phần được hover sẽ nổi lên

### Cách tính toán (trong FakeDataService.java)
```java
// Lọc transactions trong tháng hiện tại, chỉ lấy EXPENSE
Map<String, BigDecimal> byCat = transactions.stream()
    .filter(t -> !t.getDate().isBefore(start) && !t.getDate().isAfter(end))
    .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
    .collect(Collectors.groupingBy(
        t -> t.getCategory().getName(),
        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
    ));
```

### Ví dụ Output
```
Food & Drinks: $250.50 (35.2%)
Transport: $120.75 (17.0%)
Shopping: $180.25 (25.3%)
Entertainment: $95.00 (13.4%)
Others: $65.50 (9.1%)
```

---

## 📈 2. Cash Flow Trend (Line Chart)

### Mục đích
Hiển thị xu hướng thu nhập và chi tiêu trong 6 tháng gần nhất.

### Dữ liệu đầu vào
```javascript
months: ['May 24', 'Jun 24', 'Jul 24', 'Aug 24', 'Sep 24', 'Oct 24']
incomes: [3500, 3800, 3600, 3900, 3700, 4000]
expenses: [2200, 2500, 2300, 2400, 2600, 2100]
```

### Tính năng
- ✅ **2 đường line**: Income (xanh lá) và Expense (đỏ)
- ✅ **Fill area**: Vùng dưới đường có màu nhạt
- ✅ **Smooth curve**: Đường cong mượt (tension: 0.4)
- ✅ **Point markers**: Điểm dữ liệu rõ ràng
- ✅ **Hover interaction**: Hiển thị cả 2 giá trị cùng lúc
- ✅ **Grid lines**: Chỉ hiển thị ở trục Y
- ✅ **Currency format**: Trục Y hiển thị ký hiệu $

### Cách tính toán (trong FakeDataService.java)
```java
for (int i=5; i>=0; i--) {
    YearMonth ymi = YearMonth.now().minusMonths(i);
    months.add(ymi.getMonth().toString().substring(0,3) + " " + ymi.getYear().substring(2));
    
    LocalDate s = ymi.atDay(1);
    LocalDate e = ymi.atEndOfMonth();
    
    inc.add(sumByTypeBetween(CategoryType.INCOME, s, e));
    exp.add(sumByTypeBetween(CategoryType.EXPENSE, s, e));
}
```

### Ví dụ Output
```
May 24: Income $3,500 | Expense $2,200 | Net +$1,300
Jun 24: Income $3,800 | Expense $2,500 | Net +$1,300
Jul 24: Income $3,600 | Expense $2,300 | Net +$1,300
...
```

---

## 🎨 Customization (Tùy chỉnh)

### Màu sắc Doughnut Chart
```javascript
const colors = [
  '#6366f1',  // Indigo
  '#8b5cf6',  // Purple
  '#ec4899',  // Pink
  '#f43f5e',  // Rose
  '#f59e0b',  // Amber
  '#10b981',  // Green
  '#3b82f6',  // Blue
  '#06b6d4',  // Cyan
  '#14b8a6',  // Teal
  '#84cc16'   // Lime
];
```

### Thay đổi kích thước Doughnut hole
```javascript
cutout: '65%'  // 0% = pie chart, 100% = invisible
```

### Thay đổi độ cong của Line Chart
```javascript
tension: 0.4  // 0 = straight lines, 1 = very curvy
```

---

## 🔧 Xử lý Lỗi

### Empty Data Handling
Cả 2 functions đều có xử lý khi không có dữ liệu:
```javascript
if (!labels || labels.length === 0) {
  console.warn('No data available');
  ctx.parentElement.innerHTML += '<p>No data available</p>';
  return;
}
```

### Try-Catch Block
```javascript
try {
  new Chart(ctx, {...});
} catch (error) {
  console.error('Error rendering chart:', error);
}
```

---

## 📱 Responsive Design

### Chart.js Configuration
```javascript
options: {
  responsive: true,
  maintainAspectRatio: true,
  ...
}
```

### Canvas Height
```html
<canvas id="catChart" height="240"></canvas>
<canvas id="flowChart" height="240"></canvas>
```

---

## 🧪 Testing

### Test URL
Truy cập: `http://localhost:8080/chart-test.html`

### Browser Console
Mở Developer Tools (F12) để xem:
- Console logs
- Data being passed
- Any errors

### Debug Script trong dashboard.html
```javascript
console.log('Category Labels:', catLabels);
console.log('Category Values:', catValues);
console.log('Months:', months);
console.log('Incomes:', incomes);
console.log('Expenses:', expenses);
```

---

## 🎯 Best Practices

### 1. Data Validation
- ✅ Kiểm tra null/undefined
- ✅ Kiểm tra array length > 0
- ✅ Convert BigDecimal to number nếu cần

### 2. Performance
- ✅ Sử dụng canvas thay vì SVG cho charts lớn
- ✅ Limit số lượng data points (6 months thay vì tất cả)
- ✅ Debounce resize events

### 3. Accessibility
- ✅ Màu sắc có contrast tốt
- ✅ Tooltips với thông tin đầy đủ
- ✅ Labels rõ ràng

### 4. User Experience
- ✅ Smooth animations
- ✅ Interactive tooltips
- ✅ Meaningful legends
- ✅ Empty states với messages

---

## 📚 Tài Liệu Chart.js

- **Official Docs**: https://www.chartjs.org/docs/latest/
- **Doughnut Chart**: https://www.chartjs.org/docs/latest/charts/doughnut.html
- **Line Chart**: https://www.chartjs.org/docs/latest/charts/line.html
- **Configuration**: https://www.chartjs.org/docs/latest/configuration/

---

## 🔮 Future Enhancements

### Có thể thêm:
1. **Bar Chart** - Chi tiêu theo tháng
2. **Radar Chart** - So sánh budget vs actual
3. **Mixed Chart** - Kết hợp line và bar
4. **Animation** - Smooth transitions khi data thay đổi
5. **Export** - Xuất chart ra PNG/SVG
6. **Date Range Filter** - Cho phép user chọn khoảng thời gian
7. **Drill-down** - Click vào category để xem chi tiết transactions
8. **Comparison** - So sánh với tháng trước

---

## ⚙️ Configuration Options

### Global Chart.js Settings
```javascript
Chart.defaults.font.family = "'Inter', sans-serif";
Chart.defaults.color = '#64748b';
Chart.defaults.borderColor = '#e2e8f0';
```

### Plugin Options
- **Legend**: Position, styling, click behavior
- **Tooltip**: Content, styling, trigger mode
- **Title**: Display, text, styling
- **Animation**: Duration, easing, delay

---

## 🎓 Troubleshooting

### Biểu đồ không hiển thị?
1. ✅ Kiểm tra Chart.js đã được load chưa (xem Network tab)
2. ✅ Kiểm tra canvas element có tồn tại không
3. ✅ Xem Console có lỗi JavaScript không
4. ✅ Verify data có đúng format không

### Dữ liệu không đúng?
1. ✅ Check FakeDataService.buildDashboard()
2. ✅ Log data trong template (console.log)
3. ✅ Verify Thymeleaf inline syntax
4. ✅ Check BigDecimal conversion

### Chart bị cắt hoặc nhỏ?
1. ✅ Adjust canvas height attribute
2. ✅ Check parent container width
3. ✅ Set maintainAspectRatio to false nếu cần
4. ✅ Add responsive: true

---

**✨ Chúc bạn có trải nghiệm tốt với các biểu đồ!**
