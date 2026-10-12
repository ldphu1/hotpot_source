// Screen: Chọn bàn
import 'package:flutter/material.dart';
import '../models/table_model.dart';
import '../services/checkin_api_service.dart';
import 'menu_screen.dart';

class TableSelectionScreen extends StatefulWidget {
  const TableSelectionScreen({super.key});

  @override
  State<TableSelectionScreen> createState() => _TableSelectionScreenState();
}

class _TableSelectionScreenState extends State<TableSelectionScreen> {
  final CheckInApiService _apiService = CheckInApiService();
  int? _selectedFloorId;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8F9FA),
      appBar: AppBar(
        title: const Text('Chọn Bàn Four Spice', style: TextStyle(fontWeight: FontWeight.bold)),
        backgroundColor: const Color(0xFFD32F2F),
        foregroundColor: Colors.white,
        elevation: 0,
        centerTitle: true,
      ),
      body: FutureBuilder<List<dynamic>>(
        future: Future.wait([
          _apiService.getDanhSachBan(),
          _apiService.getAllKhuVuc(),
        ]),
        builder: (context, AsyncSnapshot<List<dynamic>> snapshot) {
          if (snapshot.connectionState == ConnectionState.waiting) {
            return const Center(child: CircularProgressIndicator());
          } else if (snapshot.hasError) {
            return Center(child: Text('Lỗi: ${snapshot.error}'));
          } else if (!snapshot.hasData || snapshot.data![0].isEmpty) {
            return const Center(child: Text('Không có dữ liệu bàn'));
          }

          final allTables = snapshot.data![0] as List<TableModel>;
          final rawFloors = List<Map<String, dynamic>>.from(snapshot.data![1]);
          
          final Map<int, String> floors = {};
          for (var kv in rawFloors) {
            if (kv['id'] != null) {
              floors[kv['id']] = kv['tenKhuVuc'] ?? 'Khu vực ${kv['id']}';
            }
          }
          final sortedFloorIds = floors.keys.toList()..sort();
          if (_selectedFloorId == null && sortedFloorIds.isNotEmpty) {
            WidgetsBinding.instance.addPostFrameCallback((_) {
              if (mounted) {
                setState(() {
                  _selectedFloorId = sortedFloorIds.first;
                });
              }
            });
          }

          final displayTables = _selectedFloorId != null 
              ? allTables.where((t) => t.khuVucId == _selectedFloorId).toList()
              : allTables;

          return Column(
            children: [
              if (sortedFloorIds.isNotEmpty)
                Container(
                  height: 60,
                  padding: const EdgeInsets.symmetric(vertical: 10),
                  child: ListView.builder(
                    scrollDirection: Axis.horizontal,
                    padding: const EdgeInsets.symmetric(horizontal: 10),
                    itemCount: sortedFloorIds.length,
                    itemBuilder: (context, index) {
                      final floorId = sortedFloorIds[index];
                      final isSelected = floorId == _selectedFloorId;
                      return GestureDetector(
                        onTap: () {
                          setState(() {
                            _selectedFloorId = floorId;
                          });
                        },
                        child: Container(
                          margin: const EdgeInsets.only(right: 10),
                          padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
                          decoration: BoxDecoration(
                            color: isSelected ? const Color(0xFFD32F2F) : Colors.white,
                            borderRadius: BorderRadius.circular(20),
                            border: Border.all(color: const Color(0xFFD32F2F)),
                          ),
                          alignment: Alignment.center,
                          child: Text(
                            floors[floorId] ?? 'Khu vực $floorId',
                            style: TextStyle(
                              color: isSelected ? Colors.white : const Color(0xFFD32F2F),
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      );
                    },
                  ),
                ),
              Expanded(
                child: GridView.builder(
                  padding: const EdgeInsets.all(10),
                  gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                    crossAxisCount: 3,
                    childAspectRatio: 1.0,
                    crossAxisSpacing: 10,
                    mainAxisSpacing: 10,
                  ),
                  itemCount: displayTables.length,
                  itemBuilder: (context, index) {
                    final table = displayTables[index];
                    Color bgColor;
                    Color iconColor;
                    String statusText;

                    switch (table.trangThaiBan) {
                      case 3:
                        bgColor = Colors.red.shade50;
                        iconColor = Colors.red.shade700;
                        statusText = 'Đang phục vụ';
                        break;
                      case 2:
                        bgColor = Colors.blue.shade50;
                        iconColor = Colors.blue.shade700;
                        statusText = 'Đã đặt';
                        break;
                      case 4:
                        bgColor = Colors.orange.shade50;
                        iconColor = Colors.orange.shade700;
                        statusText = 'Chờ dọn';
                        break;
                      default:
                        bgColor = Colors.green.shade50;
                        iconColor = Colors.green.shade700;
                        statusText = 'Bàn trống';
                        break;
                    }

                    final tableNameDisplay = table.khuVucId != null 
                        ? 'T${table.khuVucId} - Bàn ${table.tenBanAn}'
                        : 'Bàn ${table.tenBanAn}';

                    return InkWell(
                      onTap: () {
                        if (table.trangThaiBan == 3 && table.idHoaDon != null) {
                          Navigator.push(
                            context,
                            MaterialPageRoute(
                              builder: (context) => MenuScreen(
                                tableId: table.idBanAn,
                                tableName: table.tenBanAn,
                                invoiceId: table.idHoaDon,
                              ),
                            ),
                          );
                        } else {
                          ScaffoldMessenger.of(context).showSnackBar(
                            const SnackBar(content: Text('Bàn này chưa được mở! Vui lòng liên hệ nhân viên để vào bàn.')),
                          );
                        }
                      },
                      child: Container(
                        decoration: BoxDecoration(
                          color: Colors.white,
                          borderRadius: BorderRadius.circular(16),
                          border: Border.all(color: bgColor, width: 2),
                          boxShadow: [
                            BoxShadow(
                              color: bgColor.withValues(alpha: 0.4),
                              blurRadius: 8,
                              offset: const Offset(0, 4),
                            )
                          ],
                        ),
                        child: Column(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Container(
                              padding: const EdgeInsets.all(12),
                              decoration: BoxDecoration(
                                color: bgColor,
                                shape: BoxShape.circle,
                              ),
                              child: Icon(Icons.table_restaurant, color: iconColor, size: 32),
                            ),
                            const SizedBox(height: 12),
                            Text(
                              tableNameDisplay,
                              style: const TextStyle(color: Colors.black87, fontWeight: FontWeight.bold, fontSize: 16),
                              textAlign: TextAlign.center,
                              maxLines: 2,
                            ),
                            const SizedBox(height: 4),
                            Text(
                              statusText,
                              style: TextStyle(color: iconColor, fontSize: 12, fontWeight: FontWeight.w600),
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          );
        },
      ),
    );
  }
}