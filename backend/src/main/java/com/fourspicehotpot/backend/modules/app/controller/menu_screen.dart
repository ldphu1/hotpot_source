import 'package:flutter/material.dart';
import '../models/menu_item.dart';
import '../services/checkin_api_service.dart';

class MenuScreen extends StatefulWidget {
  final int? tableId;
  final String? tableName;
  final int? invoiceId;

  const MenuScreen({super.key, this.tableId, this.tableName, this.invoiceId});

  @override
  State<MenuScreen> createState() => _MenuScreenState();
}

class _MenuScreenState extends State<MenuScreen> {
  final CheckInApiService _apiService = CheckInApiService();

  @override
  Widget build(BuildContext context) {
    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: Text(widget.tableName != null ? 'Bàn ${widget.tableName} - Gọi món' : 'Thực Đơn'),
          backgroundColor: Colors.red[800],
          foregroundColor: Colors.white,
          bottom: const TabBar(
            labelColor: Colors.white,
            unselectedLabelColor: Colors.white70,
            indicatorColor: Colors.white,
            tabs: [
              Tab(text: 'Tất cả'),
              Tab(text: 'Món lẻ'),
              Tab(text: 'Set lẩu'),
            ],
          ),
        ),
        body: TabBarView(
          children: [
            _buildMenuTab(_apiService.getAllThucDon()),
            _buildMenuTab(_apiService.getAllMonLe()),
            _buildMenuTab(_apiService.getAllSetLau()),
          ],
        ),
      ),
    );
  }

  void _datMon(MenuItem item) async {
    if (widget.invoiceId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Vui lòng chọn bàn trước khi gọi món!')),
      );
      return;
    }
    
    try {
      await _apiService.themMon(
        widget.invoiceId!, item.id!, 1, widget.tableName ?? 'Chưa xác định', widget.tableId!,
      );
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('Đã thêm ${item.tenSanPham} vào bàn ${widget.tableName}'), backgroundColor: Colors.green),
      );
    } catch (e) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('$e'), backgroundColor: Colors.red));
    }
  }

  Widget _buildMenuTab(Future<List<MenuItem>> futureFunc) {
    return FutureBuilder<List<MenuItem>>(
      future: futureFunc,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return const Center(child: CircularProgressIndicator());
        } else if (snapshot.hasError) {
          return Center(child: Text('Lỗi: ${snapshot.error}'));
        } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
          return const Center(child: Text('Không có dữ liệu thực đơn'));
        }

        final items = snapshot.data!;
        return GridView.builder(
          padding: const EdgeInsets.all(10),
          gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            crossAxisCount: 3,
            childAspectRatio: 0.75, 
            crossAxisSpacing: 8,
            mainAxisSpacing: 8,
          ),
          itemCount: items.length,
          itemBuilder: (context, index) {
            return _buildMenuItem(items[index]);
          },
        );
      },
    );
  }

  Widget _buildMenuItem(MenuItem item) {
    // Base url chứa thư mục uploads của backend (thay đổi theo cấu hình thật của bạn)
    final String imageUrl = item.hinhAnh != null 
        ? 'http://localhost:8080/uploads/${item.hinhAnh}' 
        : 'https://via.placeholder.com/150';

    return Card(
      elevation: 3,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Expanded(
            child: ClipRRect(
              borderRadius: const BorderRadius.vertical(top: Radius.circular(10)),
              child: Image.network(
                imageUrl,
                fit: BoxFit.cover,
                errorBuilder: (context, error, stackTrace) =>
                    const Icon(Icons.broken_image, size: 50, color: Colors.grey),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(6.0), 
            child: Column(
              children: [
                Text(
                  item.tenSanPham ?? 'Chưa có tên',
                  style: const TextStyle(fontWeight: FontWeight.bold, fontSize: 13),
                  maxLines: 2,
                  overflow: TextOverflow.ellipsis,
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 4),
                Text(
                  '${item.giaSauGiam ?? item.gia ?? 0} đ',
                  style: TextStyle(
                    color: Colors.red[800],
                    fontWeight: FontWeight.bold,
                    fontSize: 14, 
                  ),
                ),
                const SizedBox(height: 4),
                ElevatedButton(
                  onPressed: () => _datMon(item),
                  style: ElevatedButton.styleFrom(
                    backgroundColor: Colors.red[800],
                    foregroundColor: Colors.white,
                    minimumSize: const Size(double.infinity, 30),
                    padding: EdgeInsets.zero,
                  ),
                  child: const Text('Thêm món', style: TextStyle(fontSize: 12)),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}