# CLAUDE.md – Nhật ký thay đổi & ngữ cảnh dự án MiniCommerce

> File này dùng để **lưu lại mọi thay đổi qua từng session học** (file nào đổi, đổi gì, vì sao),
> để ở session sau (người hoặc AI như Claude Code) đọc vào là nắm được dự án đang ở trạng thái nào.
> Claude Code tự động đọc file `CLAUDE.md` ở thư mục gốc khi bắt đầu làm việc.

---

## 1. Tổng quan dự án

- **MiniCommerce**: ứng dụng console bán quần áo, Java Core thuần, **không thư viện ngoài**.
- Dữ liệu lưu trong bộ nhớ (repository in-memory), tắt chương trình là mất.
- Yêu cầu: **JDK 17+** (code dùng `record`, `Stream.toList()`, switch `->`).
- Kiến trúc: `controller` → `service` (interface + `impl`) → `repository` (interface + `impl`) → `entity`.
  Wiring thủ công trong `config/AppConfig.java`.

### Build & chạy
```bash
javac -encoding UTF-8 -d out $(find src/main/java -name "*.java")
java -cp out com.minicommerce.Main
```
> **Bắt buộc `-encoding UTF-8`**: source có comment tiếng Việt. Thiếu flag này, JDK 17 trên Windows
> (mặc định cp1252) sẽ báo lỗi `unmappable character`. IntelliJ/VS Code mặc định UTF-8 nên không bị.

---

## 1b. Bản đồ kiến thức theo syllabus (Session 3 → 7)

> Syllabus: S3 OOP & SOLID · S4 Generic & Collection · S5 Set, Map, Exception · S6 Multi-thread · S7 File & JDBC.
> Tìm trong code bằng nhãn `[Session N - ...]`. Comment cũ ghi "Session 5" nhưng nội dung là Lambda/Stream
> đã được đổi nhãn thành `[Lambda/Stream]` (kiến thức bổ sung, không thuộc S3–S7).

| Session | Đã áp dụng ở đâu (tóm tắt) |
|---|---|
| **S3 – OOP** | Encapsulation + validate trong setter (`ProductVariant`, `CartItem`); composition (`Product` → `List<ProductVariant>`, `Order` → `List<OrderItem>`); inheritance (3 exception `extends RuntimeException`); polymorphism (`implements` interface, override `toString`); enum có field/constructor (`ProductSortOption`); utility class `final` + private constructor; DTO `CheckoutRequest`. |
| **S3 – SOLID** | **S**: controller / service / repository / `ReceiptPrinter` tách trách nhiệm, `InventoryService` tách khỏi `ProductService`. **O**: `DiscountPolicy` (Strategy) – thêm policy không sửa `OrderServiceImpl`. **L**: mọi impl của `*Repository` thay thế nhau được. **I**: nhiều interface service nhỏ. **D**: service nhận interface qua constructor, `AppConfig` là composition root. |
| **S4 – Generic** | Generic method bounded `<E extends Enum<E>>` (`InputHelper.readEnum/readOptionalEnum/optionsOf`); `<T> runTask(Supplier<T>)` (`ReportServiceImpl`); `Optional<T>` ở repository; `Predicate<Product>`, `Comparator<Product>`. |
| **S4 – Collection** | `List`/`ArrayList` khai báo theo interface; defensive copy (`new ArrayList<>(items)`); `List.copyOf` immutable (`snapshotItems`, `getSentLog`); sort bằng `Comparator`. |
| **S5 – Set** | `Set.of(PENDING, CONFIRMED)` – `CANCELLABLE` trong `OrderServiceImpl`. |
| **S5 – Map** | Repository = `Map<id, entity>`; `Cart` dùng `LinkedHashMap<sku, CartItem>`; `EnumMap` (thống kê theo status); `LinkedHashMap` giữ thứ tự top-seller; `TreeMap` (lý do lỗi flash sale); `computeIfAbsent`, `merge`, `getOrDefault`, `entrySet`. |
| **S5 – Exception** | 3 custom unchecked exception; service throw – controller catch; `orElseThrow`; `try/catch NumberFormatException` nhập lại (`InputHelper`); checked `throws InterruptedException` (`FlashSaleSimulator.run`); multi-catch Timeout/Execution/Interrupted (`AdminController.dashboard`); `try/finally` unlock. |
| **S6 – Multi-thread** | Xem nhật ký Session 6 bên dưới (`ReentrantLock`, `synchronized`, `volatile`, `ConcurrentHashMap`, `ExecutorService`, `CompletableFuture`, `CountDownLatch`, `AtomicInteger`). |
| **S7 – File & JDBC** | **Chưa áp dụng.** Dữ liệu chỉ ở RAM. Hướng làm: `JdbcProductRepository implements ProductRepository` (JDBC `PreparedStatement`, try-with-resources) và xuất hoá đơn/đơn hàng ra file (`BufferedWriter`/`Files`). Nhờ DIP chỉ cần đổi 1 dòng trong `AppConfig`. |

---

## 2. Quy ước khi sửa code (áp dụng cho mọi session)

1. Đánh dấu code mới/sửa bằng comment `// [Session N] ...` giải thích **kiến thức nào được áp dụng và tại sao**.
2. Giữ nguyên kiến trúc package; tính năng mới → thêm method vào interface service trước, rồi mới implement.
3. Không thêm thư viện ngoài; phải compile được với JDK 17.
4. File source lưu UTF-8.
5. Sau mỗi session: **cập nhật mục 4 (Nhật ký)** theo mẫu ở mục 3, rồi commit git
   (`git commit -m "Session N: <chủ đề>"`).

---

## 3. Mẫu ghi nhật ký cho session mới

```markdown
### Session N – <Chủ đề> (<ngày>)
**Kiến thức áp dụng:** ...
**Tính năng mới cho người dùng:** ...

| File | Loại | Thay đổi |
|------|------|----------|
| path/to/File.java | Mới / Sửa / Xoá | Mô tả ngắn |

**Thay đổi hành vi / lưu ý:** ...
**Cách kiểm tra:** ...
```

---

## 4. Nhật ký thay đổi

### Session 0 – Code gốc (starter, trước Session 5)
Code mentor phát. Chưa có git. Repository dùng `LinkedHashMap`, logic chủ yếu viết bằng vòng `for`,
không có xử lý đa luồng.

---

### Bổ sung – Functional Programming, Lambda Expression, Stream API (trước đây ghi nhầm là "Session 5"; nhãn code: `[Lambda/Stream]`)
**Kiến thức áp dụng:** lambda, method reference (`Product::isActive`, `OrderSummary::new`,
`System.out::println`), functional interface có sẵn (`Predicate`, `Comparator`, `Supplier`) và tự định nghĩa
(`@FunctionalInterface DiscountPolicy`), kết hợp hàm (`Predicate.and`, `Comparator.thenComparing/reversed`,
default method `bestOf`), higher-order function, `Optional`, Stream API (`filter`, `map`, `flatMap`, `sorted`,
`limit`, `anyMatch`, `reduce`, `mapToLong/mapToInt`, `sum/min`), `Collectors` (`groupingBy`, `counting`,
`summingInt/Long`, `toMap`, `joining`), `record`.

**Tính năng mới cho người dùng:**
- Menu chính **9. Search & filter products**: lọc theo từ khoá, category, size, giá tối đa, còn hàng; sắp xếp theo
  tên/giá tăng/giá giảm/tồn kho.
- Danh sách sản phẩm hiển thị thêm "from <giá thấp nhất>".
- Admin **8. Sales report**: số đơn & doanh thu theo trạng thái + top 3 sản phẩm bán chạy.
- Admin **9. Low-stock report**: các biến thể có tồn kho ≤ ngưỡng nhập vào.
- **Giảm giá tự động khi checkout** (trước đây discount luôn = 0).

| File | Loại | Thay đổi |
|------|------|----------|
| `util/ProductFilters.java` | Mới | Factory trả về `Predicate<Product>`: `active`, `nameOrIdContains`, `inCategory`, `inStock`, `minPriceAtMost`, `hasSize`. |
| `util/ProductSortOption.java` | Mới | Enum, mỗi hằng giữ một `Comparator<Product>` (NAME_ASC, PRICE_ASC, PRICE_DESC, STOCK_DESC). |
| `service/DiscountPolicy.java` | Mới | `@FunctionalInterface` + static factory `none/percentOver/flatOver` + default method `bestOf` (higher-order). |
| `entity/Product.java` | Sửa | Thêm `getMinPrice()` (`OptionalLong`, `mapToLong().min()`), `getTotalStock()` (`mapToInt().sum()`). |
| `service/ProductService.java` | Sửa | Thêm `filterProducts(Predicate, Comparator)`. |
| `service/impl/ProductServiceImpl.java` | Sửa | Implement `filterProducts`; `searchProducts`/`getProductsByCategory` tái sử dụng nó; vòng for kiểm tra SKU trùng → stream `filter/findFirst/ifPresent`; `Collectors.toList()` → `toList()`. |
| `repository/impl/InMemoryProductRepository.java` | Sửa | `findBySku` viết lại: bản cũ flatMap rồi stream lồng để tìm ngược product (O(n²)), nay dùng `anyMatch`. |
| `service/InventoryService.java` + `impl/InventoryServiceImpl.java` | Sửa | Thêm `findLowStockVariants(threshold)` dùng `flatMap` + `sorted(comparingInt().thenComparing())`. |
| `service/OrderService.java` | Sửa | Thêm `countOrdersByStatus()`, `getRevenueByStatus()`, `getTopSellingProducts(limit)`. |
| `service/impl/OrderServiceImpl.java` | Sửa | Implement 3 báo cáo bằng `groupingBy` (EnumMap), `flatMap`, sort `Map.Entry`, `toMap(LinkedHashMap::new)`. `checkout`: vòng for → `stream().map(this::toOrderItem).toList()`, gộp số lượng theo SKU bằng `groupingBy + summingInt`, tính discount qua `DiscountPolicy`. Constructor nhận thêm `DiscountPolicy`. |
| `config/AppConfig.java` | Sửa | Tạo policy: `percentOver(1_000_000, 5).bestOf(flatOver(500_000, 20_000))`. |
| `controller/ProductController.java` | Sửa | Thêm `searchAndFilter()`: mỗi tiêu chí là `Optional<Predicate>`, gom bằng `flatMap(Optional::stream).reduce(p -> true, Predicate::and)`. Vòng for → `forEach(this::printProductWithVariants)`. |
| `controller/CartController.java` | Sửa | `showCart`: vòng for + biến cộng dồn → `record CartLine` + `map` + `mapToLong().sum()`. |
| `controller/OrderController.java` | Sửa | `stream().map(OrderSummary::new).forEach(System.out::println)`. |
| `controller/AdminController.java` | Sửa | Thêm menu 8 (sales report), 9 (low-stock report). |
| `util/InputHelper.java` | Sửa | Thêm `readOptionalEnum`, `readOptionalLong` (trả `Optional`), `readYesNo`, `optionsOf` (`Collectors.joining`). |
| `util/ReceiptPrinter.java` | Sửa | for-each → `forEach` + lambda. |
| `Main.java` | Sửa | Thêm lựa chọn 9 ở menu chính. |

**Thay đổi hành vi / lưu ý:**
- Đơn ≥ 500.000đ giảm 20.000đ; đơn ≥ 1.000.000đ giảm 5% (lấy mức lớn hơn). Đổi chính sách ở `AppConfig`.
- `getAllActiveProducts`/`filterProducts` trả về list **không sửa được** (`Stream.toList()`).

**Cách kiểm tra:** Menu 9 → bỏ trống hết, max price `300000`, `y`, `PRICE_DESC` → chỉ ra P002, P001.
Mua 2 × `SH-BEI-L` (638.000đ) → hoá đơn có Discount 20.000đ.

---

### Session 6 – Multi-thread
**Kiến thức áp dụng:** race condition (check-then-act), `ReentrantLock` + `try/finally`, `synchronized` block
trên object, `volatile`, collection thread-safe (`ConcurrentHashMap`, `CopyOnWriteArrayList`), atomic
(`AtomicInteger`), `ExecutorService` (fixed / single thread pool), `ThreadFactory` đặt tên thread + daemon,
`CompletableFuture` (`supplyAsync`, `allOf`, `thenApply`, `get(timeout)`), `CountDownLatch`, xử lý
`InterruptedException` đúng cách, `shutdown()/awaitTermination()`.

**Vấn đề được giải quyết:** trước đây nếu nhiều khách checkout cùng lúc, 2 thread có thể cùng đọc tồn kho = 1,
cùng thấy "đủ hàng" và cùng trừ → bán vượt kho / tồn kho sai. Ngoài ra `HashMap`/`LinkedHashMap` không an toàn
khi nhiều thread ghi.

**Tính năng mới cho người dùng:**
- Admin **10. Dashboard**: 4 báo cáo chạy song song trên thread pool (~400ms thay vì ~1600ms tuần tự), in ra
  thread nào chạy tác vụ nào.
- Admin **11. Flash sale simulation**: N khách (thread) cùng mua 1 SKU, in số thành công/thất bại và kiểm tra
  tồn kho nhất quán.
- Admin **12. Notification log**: xác nhận đơn hàng được "gửi" bất đồng bộ ở background thread sau checkout.

| File | Loại | Thay đổi |
|------|------|----------|
| `service/NotificationService.java` + `impl/NotificationServiceImpl.java` | Mới | Single-thread executor (thread `notification-worker`, daemon) gửi thông báo giả lập (sleep 300ms); log lưu trong `CopyOnWriteArrayList`; `shutdown()` có `awaitTermination`. |
| `service/ReportService.java` + `impl/ReportServiceImpl.java` | Mới | `buildDashboardAsync()`: 4 `CompletableFuture.supplyAsync` trên fixed pool 4 thread, gom bằng `allOf().thenApply()`. `ThreadFactory` dùng `AtomicInteger` đặt tên `report-worker-N`. |
| `dto/DashboardReport.java` | Mới | `record` immutable chứa kết quả dashboard. |
| `simulation/FlashSaleSimulator.java` | Mới (package mới) | Giả lập N khách đồng thời: `ExecutorService`, `CountDownLatch` start gate + finished, `AtomicInteger` đếm, `ConcurrentHashMap.merge` thống kê lỗi. `Result.isConsistent()` kiểm tra `before - succeeded == after`. |
| `service/InventoryService.java` | Sửa | Thêm `reserveAll(Map<sku, qty>)` (all-or-nothing) và `releaseAll(...)`. |
| `service/impl/InventoryServiceImpl.java` | Sửa | Mọi thao tác đọc-sửa-ghi kho đi qua một `ReentrantLock`. `reserveAll` kiểm tra hết rồi mới trừ trong cùng một lock. |
| `service/CartService.java` | Sửa | Thêm `snapshotItems(userId)` (bản sao giỏ để đọc an toàn). |
| `service/impl/CartServiceImpl.java` | Sửa | `HashMap` → `ConcurrentHashMap` (`computeIfAbsent` nguyên tử); `synchronized (cart)` khi sửa/đọc giỏ của từng user. |
| `service/impl/OrderServiceImpl.java` | Sửa | `checkout` dùng `snapshotItems` + `inventoryService.reserveAll` (thay cho check rồi decrease từng SKU), gọi `notificationService.sendOrderConfirmationAsync`. `cancelOrder`/`updateOrderStatus` có `synchronized (order)` để không hoàn kho 2 lần. Constructor nhận thêm `NotificationService`. |
| `repository/impl/InMemory*Repository.java` (3 file) | Sửa | `LinkedHashMap` → `ConcurrentHashMap`. Vì mất thứ tự chèn nên `findAll` sắp theo ID (product) / `createdAt` (order). |
| `entity/ProductVariant.java` | Sửa | `stockQuantity` thành `volatile`. |
| `entity/Order.java` | Sửa | `status` thành `volatile`. |
| `config/AppConfig.java` | Sửa | Tạo `NotificationService`, `ReportService`, `FlashSaleSimulator`; thêm `shutdown()` tắt các thread pool. |
| `controller/AdminController.java` | Sửa | Constructor nhận thêm 3 dependency; menu 10, 11, 12; xử lý `TimeoutException`/`ExecutionException`/`InterruptedException`. |
| `Main.java` | Sửa | Truyền dependency mới cho `AdminController`; gọi `config.shutdown()` trước khi thoát. |

**Thay đổi hành vi / lưu ý:**
- Đơn của flash sale (user `SIM-USER-*`) được lưu thật vào danh sách đơn và trừ kho thật → chạy xong có thể
  restock lại (admin menu 4).
- `checkout` vẫn `clearCart` cả giỏ sau khi đặt; nếu cùng user thêm hàng vào giỏ đúng lúc đang checkout thì món
  thêm sau có thể bị xoá (chấp nhận được ở app console 1 người dùng).
- Đã stress test: 30 lượt × 200 khách đồng thời, tồn kho luôn nhất quán, 0 lần bán vượt.

**Cách kiểm tra:** Admin → 11 → `DR-RED-S`, 20 khách → Succeeded 6, Failed 14, "stock is consistent".
Admin → 10 → "Built in ~400 ms". Checkout một đơn, đợi 1 giây → Admin → 12 thấy dòng `notification-worker`.

---

## 5. Ý tưởng cho session sau
- Thêm unit test (JUnit) khi được học – đặc biệt test đa luồng cho `InventoryServiceImpl`.
- Đăng nhập thật bằng `AuthService` thay cho `DEFAULT_USER_ID = "USR-DEMO"` hard-code.
- Chuyển sang Maven/Gradle để quản lý build.
