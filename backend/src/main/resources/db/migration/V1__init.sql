
CREATE TABLE [dbo].[ban_an](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khu_vuc] [bigint] NOT NULL,
    [ma_ban_an] [nvarchar](50) NULL,
    [ten_ban_an] [nvarchar](100) NOT NULL,
    [suc_chua_toi_da] [int] NOT NULL,
    [trang_thai] [int] NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[ca_lam]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[ca_lam](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ten_ca_lam] [nvarchar](100) NOT NULL,
    [thoi_gian_bat_dau] [time](7) NOT NULL,
    [thoi_gian_ket_thuc] [time](7) NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[danh_gia]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[danh_gia](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [id_hoa_don] [bigint] NOT NULL,
    [so_sao] [int] NOT NULL,
    [noi_dung] [nvarchar](1000) NULL,
    [hinh_anh_dinh_kem] [nvarchar](255) NULL,
    [ngay_danh_gia] [datetime2](7) NULL,
    [trang_thai] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[danh_muc]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[danh_muc](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_danh_muc] [nvarchar](50) NULL,
    [ten_danh_muc] [nvarchar](150) NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[danh_muc_chi_tiet]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[danh_muc_chi_tiet](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_danh_muc] [bigint] NOT NULL,
    [ma_danh_muc_chi_tiet] [nvarchar](50) NULL,
    [ten_danh_muc_chi_tiet] [nvarchar](100) NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[danh_muc_dinh_luong]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[danh_muc_dinh_luong](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_danh_muc] [bigint] NOT NULL,
    [id_dinh_luong] [bigint] NOT NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[dat_ban]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[dat_ban](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [id_ban_an] [bigint] NOT NULL,
    [ma_dat_ban] [nvarchar](50) NULL,
    [thoi_gian_dat] [datetime2](7) NOT NULL,
    [so_nguoi_toi_da] [int] NOT NULL,
    [ghi_chu] [nvarchar](255) NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [hinh_thuc_dat] [int] NOT NULL,
    [trang_thai] [varchar](20) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[dia_chi]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[dia_chi](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [dia_chi_cu_the] [nvarchar](255) NOT NULL,
    [thanh_pho] [nvarchar](100) NULL,
    [quan_huyen] [nvarchar](100) NULL,
    [phuong_xa] [nvarchar](100) NULL,
    [ghi_chu] [nvarchar](255) NULL,
    [trang_thai] [int] NULL,
    [mac_dinh] [bit] NOT NULL,
    [ho_ten_nguoi_nhan] [nvarchar](100) NULL,
    [sdt_nguoi_nhan] [nvarchar](15) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[dinh_luong]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[dinh_luong](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_dinh_luong] [varchar](50) NULL,
    [gia_tri] [decimal](10, 2) NOT NULL,
    [id_don_vi] [bigint] NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[don_vi]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[don_vi](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_don_vi] [varchar](50) NULL,
    [ten_don_vi] [nvarchar](100) NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[giao_ca]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[giao_ca](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_nhan_vien_giao] [bigint] NOT NULL,
    [id_nhan_vien_nhan] [bigint] NOT NULL,
    [id_ca_lam] [bigint] NOT NULL,
    [thoi_gian_giao] [datetime2](7) NULL,
    [ghi_chu] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[hinh_anh]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[hinh_anh](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_mon_an] [bigint] NOT NULL,
    [url] [nvarchar](255) NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    [id_san_pham] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[hinh_anh_nuoc_lau]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[hinh_anh_nuoc_lau](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_nuoc_lau] [bigint] NOT NULL,
    [url] [nvarchar](255) NOT NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[hoa_don]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[hoa_don](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_phieu_giam_gia] [bigint] NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [id_nhan_vien] [bigint] NULL,
    [id_dat_ban] [bigint] NULL,
    [ma_hoa_don] [nvarchar](50) NULL,
    [loai_hoa_don] [int] NOT NULL,
    [thoi_gian_tao] [datetime2](7) NULL,
    [thoi_gian_thanh_toan] [datetime2](7) NULL,
    [tien_coc] [decimal](18, 2) NULL,
    [tien_thue] [decimal](18, 2) NULL,
    [ten_nguoi_nhan] [nvarchar](100) NULL,
    [so_dien_thoai] [nvarchar](15) NULL,
    [dia_chi_giao] [nvarchar](255) NULL,
    [phi_van_chuyen] [decimal](18, 2) NULL,
    [so_tien_duoc_giam] [decimal](18, 2) NULL,
    [tong_tien] [decimal](18, 2) NOT NULL,
    [trang_thai] [int] NULL,
    [ghi_chu] [nvarchar](255) NULL,
    [nguoi_tao] [bigint] NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [id_ban_an] [bigint] NULL,
    [thoi_gian_mo] [datetime] NULL,
    [thoi_gian_dong] [datetime] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[hoa_don_ban_an]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[hoa_don_ban_an](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_hoa_don] [bigint] NULL,
    [id_ban_an] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[hoa_don_chi_tiet]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[hoa_don_chi_tiet](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_hoa_don] [bigint] NOT NULL,
    [id_mon_an_chi_tiet] [bigint] NOT NULL,
    [so_luong] [int] NOT NULL,
    [don_gia] [decimal](18, 2) NOT NULL,
    [thanh_tien] [decimal](18, 2) NOT NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [trang_thai] [int] NULL,
    [id_san_pham] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[khach_hang]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[khach_hang](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_khach_hang] [nvarchar](50) NULL,
    [mat_khau] [nvarchar](255) NOT NULL,
    [ten_khach_hang] [nvarchar](100) NOT NULL,
    [dia_chi] [nvarchar](255) NULL,
    [so_dien_thoai] [nvarchar](15) NULL,
    [email] [nvarchar](100) NULL,
    [ngay_sinh] [date] NULL,
    [gioi_tinh] [bit] NULL,
    [trang_thai] [int] NULL,
    [anh_dai_dien] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[khach_hang_giam_gia]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[khach_hang_giam_gia](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [id_phieu_giam_gia] [bigint] NOT NULL,
    [thoi_diem_giam_gia] [datetime2](7) NULL,
    [ten_khach_hang] [nvarchar](100) NULL,
    [so_dien_thoai] [nvarchar](15) NULL,
    [email] [nvarchar](100) NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [tao_boi] [bigint] NULL,
    [cap_nhat_boi] [bigint] NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[khu_vuc]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[khu_vuc](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_khu_vuc] [nvarchar](50) NULL,
    [ten_khu_vuc] [nvarchar](150) NOT NULL,
    [vi_tri] [nvarchar](150) NULL,
    [mo_ta] [nvarchar](255) NULL,
    [trang_thai] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[lich_lam_viec]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[lich_lam_viec](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_nhan_vien] [bigint] NOT NULL,
    [ngay] [date] NOT NULL,
    [vi_tri] [nvarchar](100) NULL,
    [gio_bat_dau] [time](7) NOT NULL,
    [gio_ket_thuc] [time](7) NOT NULL,
    [ghi_chu] [nvarchar](255) NULL,
    [trang_thai] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[lich_su_hoa_don]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[lich_su_hoa_don](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_hoa_don] [bigint] NOT NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [thoi_gian] [datetime2](7) NULL,
    [hanh_dong] [nvarchar](100) NOT NULL,
    [trang_thai] [int] NULL,
    [ghi_chu] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[lich_su_thanh_toan]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[lich_su_thanh_toan](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_hoa_don] [bigint] NOT NULL,
    [ma_giao_dich] [nvarchar](100) NULL,
    [so_tien] [decimal](18, 2) NOT NULL,
    [phuong_thuc] [nvarchar](50) NOT NULL,
    [trang_thai] [int] NULL,
    [thoi_gian] [datetime2](7) NULL,
    [ghi_chu] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[mon_an]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[mon_an](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_danh_muc_chi_tiet] [bigint] NOT NULL,
    [ma_mon_an] [nvarchar](50) NULL,
    [ten_mon_an] [nvarchar](150) NOT NULL,
    [mo_ta] [nvarchar](1000) NULL,
    [hinh_anh] [nvarchar](255) NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [nguoi_tao] [bigint] NULL,
    [nguoi_cap_nhat] [bigint] NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[mon_an_chi_tiet]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[mon_an_chi_tiet](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_mon_an] [bigint] NOT NULL,
    [ma_mon_chi_tiet] [nvarchar](50) NULL,
    [ten_mon_chi_tiet] [nvarchar](150) NOT NULL,
    [gia_ban] [decimal](18, 2) NOT NULL,
    [size] [nvarchar](20) NULL,
    [don_vi] [nvarchar](20) NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [nguoi_tao] [nvarchar](100) NULL,
    [nguoi_cap_nhat] [nvarchar](100) NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[nhan_vien]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[nhan_vien](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [tai_khoan] [nvarchar](50) NOT NULL,
    [mat_khau] [nvarchar](255) NOT NULL,
    [ma_nhan_vien] [nvarchar](50) NULL,
    [ten_nhan_vien] [nvarchar](100) NOT NULL,
    [anh_nhan_vien] [nvarchar](255) NULL,
    [ngay_sinh] [date] NULL,
    [so_dien_thoai] [nvarchar](15) NULL,
    [gioi_tinh] [bit] NULL,
    [email] [nvarchar](100) NULL,
    [can_cuoc_cong_dan] [nvarchar](20) NULL,
    [chuc_vu] [int] NULL,
    [trang_thai] [int] NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [tao_boi] [bigint] NULL,
    [cap_nhat_boi] [bigint] NULL,
    [trang_thai_xoa] [bit] NULL,
    [tinh_thanh] [nvarchar](100) NULL,
    [xa_phuong] [nvarchar](100) NULL,
    [dia_chi_chi_tiet] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[nuoc_lau]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[nuoc_lau](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_nuoc_lau] [nvarchar](50) NULL,
    [ten_nuoc_lau] [nvarchar](150) NOT NULL,
    [mo_ta] [nvarchar](255) NULL,
    [gia_phu_thu] [decimal](18, 2) NULL,
    [trang_thai_xoa] [bit] NULL,
    [hinh_anh] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[permissions]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[permissions](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ten_permission] [nvarchar](100) NOT NULL,
    [mo_ta] [nvarchar](255) NULL,
    [trang_thai] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[phieu_giam_gia]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[phieu_giam_gia](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_giam_gia] [nvarchar](50) NULL,
    [ten_giam_gia] [nvarchar](255) NOT NULL,
    [so_luong] [int] NOT NULL,
    [kieu_giam_gia] [int] NOT NULL,
    [phuong_thuc_giam_gia] [int] NOT NULL,
    [dieu_kien_su_dung] [decimal](18, 2) NULL,
    [gia_tri_giam] [decimal](18, 2) NOT NULL,
    [giam_toi_da] [decimal](18, 2) NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [tao_boi] [bigint] NULL,
    [cap_nhat_boi] [bigint] NULL,
    [trang_thai_xoa] [bit] NULL,
    [ngay_bat_dau] [datetime2](7) NOT NULL,
    [ngay_ket_thuc] [datetime2](7) NOT NULL,
    [trang_thai] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[phuong_thuc_thanh_toan]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[phuong_thuc_thanh_toan](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [ma_phuong_thuc] [nvarchar](50) NULL,
    [ten_phuong_thuc] [nvarchar](100) NOT NULL,
    [trang_thai] [int] NULL,
    [ghi_chu] [nvarchar](255) NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[role_permissions]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[role_permissions](
    [role_id] [int] NOT NULL,
    [permission_id] [bigint] NOT NULL,
     PRIMARY KEY CLUSTERED
    (
    [role_id] ASC,
[permission_id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[san_pham]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[san_pham](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_danh_muc] [bigint] NOT NULL,
    [id_nuoc_lau] [bigint] NULL,
    [ma_san_pham] [nvarchar](50) NULL,
    [ten_san_pham] [nvarchar](150) NOT NULL,
    [gia] [decimal](18, 2) NOT NULL,
    [mo_ta] [nvarchar](500) NULL,
    [la_set] [bit] NULL,
    [trang_thai] [bit] NULL,
    [trang_thai_xoa] [bit] NULL,
    [ngay_tao] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [hinh_anh] [nvarchar](500) NULL,
    [id_dinh_luong] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[set_lau]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[set_lau](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_nuoc_lau] [bigint] NOT NULL,
    [ma_set_lau] [nvarchar](50) NULL,
    [ten_set] [nvarchar](150) NOT NULL,
    [gia_set] [decimal](18, 2) NOT NULL,
    [mo_ta] [nvarchar](500) NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[set_lau_chi_tiet]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[set_lau_chi_tiet](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_set_lau] [bigint] NOT NULL,
    [id_mon_an_chi_tiet] [bigint] NOT NULL,
    [mo_ta] [nvarchar](255) NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[set_thanh_phan]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[set_thanh_phan](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_set] [bigint] NOT NULL,
    [id_san_pham_thanh_phan] [bigint] NOT NULL,
    [so_luong] [int] NULL,
    [trang_thai_xoa] [bit] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[thanh_toan]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[thanh_toan](
    [id] [bigint] IDENTITY(1,1) NOT NULL,
    [id_hoa_don] [bigint] NOT NULL,
    [phuong_thuc_thanh_toan_id] [bigint] NOT NULL,
    [ma_thanh_toan] [nvarchar](50) NULL,
    [tong_tien] [decimal](18, 2) NOT NULL,
    [ngay_thanh_toan] [datetime2](7) NULL,
    [ngay_cap_nhat] [datetime2](7) NULL,
    [trang_thai] [int] NULL,
    PRIMARY KEY CLUSTERED
(
[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

/****** Object:  Table [dbo].[users_token]    Script Date: 3/10/2026 9:55:54 AM ******/
    SET ANSI_NULLS ON

    SET QUOTED_IDENTIFIER ON

CREATE TABLE [dbo].[users_token](
    [id_user_token] [bigint] IDENTITY(1,1) NOT NULL,
    [id_khach_hang] [bigint] NOT NULL,
    [token] [nvarchar](255) NOT NULL,
    [type_token] [nvarchar](50) NULL,
    [thoi_gian_het_han] [datetime2](7) NOT NULL,
    [nguoi_tao] [bigint] NULL,
    [trang_thai] [int] NULL,
    [id_nhan_vien] [bigint] NULL,
    PRIMARY KEY CLUSTERED
(
[id_user_token] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
    ) ON [PRIMARY]

    SET IDENTITY_INSERT [dbo].[ban_an] ON

    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (1, 2, N'BA-G7A9', N'01', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-03-04T11:15:50.0160525' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (2, 1, N'BA-L4O3', N'02', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-03-04T11:15:33.0250756' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (3, 1, N'BA-I6M3', N'03', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-03-04T11:15:52.9742791' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (4, 1, N'BA-D3I0', N'04', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-03-04T11:15:55.7463239' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (5, 1, N'BA-F0D5', N'05', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-02-12T18:25:24.0098667' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (6, 1, N'BA-N5O9', N'06', 4, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (7, 1, N'BA-T2Z6', N'07', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (8, 1, N'BA-F0L6', N'08', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (9, 1, N'BA-X1N5', N'09', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (10, 1, N'BA-X8T2', N'10', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (11, 1, N'BA-F9Z2', N'11', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (12, 1, N'BA-Y3E6', N'12', 6, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (13, 1, N'BA-H3X7', N'13', 8, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (14, 1, N'BA-B6C1', N'14', 8, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (15, 1, N'BA-W5F3', N'15', 8, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (16, 1, N'BA-H4C0', N'16', 16, 1, CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2), CAST(N'2026-01-29T17:03:43.6940698' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (17, 2, N'BA-B4Q3', N'01', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (18, 2, N'BA-F7U2', N'02', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (19, 2, N'BA-W1Y8', N'03', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (20, 2, N'BA-O2W6', N'04', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (21, 2, N'BA-E8Q4', N'05', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (22, 2, N'BA-B2P0', N'06', 4, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (23, 2, N'BA-M3P6', N'07', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (24, 2, N'BA-N3P3', N'08', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (25, 2, N'BA-Z8T9', N'09', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (26, 2, N'BA-Q3S6', N'10', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (27, 2, N'BA-M8B6', N'11', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (28, 2, N'BA-K1G6', N'12', 6, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (29, 2, N'BA-V5J3', N'13', 8, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (30, 2, N'BA-N1Q8', N'14', 8, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (31, 2, N'BA-V0Y2', N'15', 8, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (32, 2, N'BA-C3R2', N'16', 16, 1, CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2), CAST(N'2026-01-29T17:04:08.8612823' AS DateTime2))
    INSERT [dbo].[ban_an] ([id], [id_khu_vuc], [ma_ban_an], [ten_ban_an], [suc_chua_toi_da], [trang_thai], [ngay_tao], [ngay_cap_nhat]) VALUES (36, 1, N'BA-B8V9', N'30', 4, 1, CAST(N'2026-03-07T18:26:22.4421456' AS DateTime2), CAST(N'2026-03-07T18:26:22.4421456' AS DateTime2))
    SET IDENTITY_INSERT [dbo].[ban_an] OFF

    SET IDENTITY_INSERT [dbo].[ca_lam] ON

    INSERT [dbo].[ca_lam] ([id], [ten_ca_lam], [thoi_gian_bat_dau], [thoi_gian_ket_thuc]) VALUES (1, N'Ca sáng', CAST(N'09:00:00' AS Time), CAST(N'14:00:00' AS Time))
    INSERT [dbo].[ca_lam] ([id], [ten_ca_lam], [thoi_gian_bat_dau], [thoi_gian_ket_thuc]) VALUES (2, N'Ca chiều', CAST(N'14:00:00' AS Time), CAST(N'18:00:00' AS Time))
    INSERT [dbo].[ca_lam] ([id], [ten_ca_lam], [thoi_gian_bat_dau], [thoi_gian_ket_thuc]) VALUES (3, N'Ca tối', CAST(N'18:00:00' AS Time), CAST(N'23:00:00' AS Time))
    SET IDENTITY_INSERT [dbo].[ca_lam] OFF

    SET IDENTITY_INSERT [dbo].[danh_gia] ON

    INSERT [dbo].[danh_gia] ([id], [id_khach_hang], [id_hoa_don], [so_sao], [noi_dung], [hinh_anh_dinh_kem], [ngay_danh_gia], [trang_thai]) VALUES (1, 1, 1, 5, N'Không gian sạch sẽ, đồ ăn nn, phục vụ nhiệt tình', N'danhgia1.jpg', CAST(N'2026-01-29T17:07:25.3170750' AS DateTime2), 1)
    INSERT [dbo].[danh_gia] ([id], [id_khach_hang], [id_hoa_don], [so_sao], [noi_dung], [hinh_anh_dinh_kem], [ngay_danh_gia], [trang_thai]) VALUES (2, 2, 2, 4, N'Lẩu đậm vị, lên món hơi chậm một chút', N'danhgia2.jpg', CAST(N'2026-01-29T17:07:25.3170750' AS DateTime2), 1)
    INSERT [dbo].[danh_gia] ([id], [id_khach_hang], [id_hoa_don], [so_sao], [noi_dung], [hinh_anh_dinh_kem], [ngay_danh_gia], [trang_thai]) VALUES (3, 3, 3, 5, N'Đi đông người rất phù hợp, bàn rộng, nhân viên chu đáo', N'danhgia3.jpg', CAST(N'2026-01-29T17:07:25.3170750' AS DateTime2), 1)
    INSERT [dbo].[danh_gia] ([id], [id_khach_hang], [id_hoa_don], [so_sao], [noi_dung], [hinh_anh_dinh_kem], [ngay_danh_gia], [trang_thai]) VALUES (4, 4, 4, 3, N'Giao hàng nhanh nhưng nước lẩu hơi nhạt', N'danhgia4.jpg', CAST(N'2026-01-29T17:07:25.3170750' AS DateTime2), 1)
    INSERT [dbo].[danh_gia] ([id], [id_khach_hang], [id_hoa_don], [so_sao], [noi_dung], [hinh_anh_dinh_kem], [ngay_danh_gia], [trang_thai]) VALUES (5, 5, 5, 5, N'Rất hài lòng, sẽ quay lại lần sau', N'danhgia5.jpg', CAST(N'2026-01-29T17:07:25.3170750' AS DateTime2), 1)
    SET IDENTITY_INSERT [dbo].[danh_gia] OFF

    SET IDENTITY_INSERT [dbo].[danh_muc] ON

    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (1, N'DM-G6V1', N'Lẩu', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (2, N'DM-B9G0', N'Đồ uống', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (3, N'DM-Z2O3', N'Đồ nhúng lẩu', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (4, N'DM-P8S3', N'Đồ tráng miệng', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (5, N'DM-H8O9', N'Đồ ăn kèm', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (6, N'DM-L8O7', N'Nước lẩu', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (37, N'DM-G0G1', N'Cá lóc', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (38, N'DM-N8B8', N'test', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (39, N'DM-J6J1', N'test select2', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (40, N'DM-M6W4', N'sàdasdasàdsdasd', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (41, N'DM-C9W3', N'aaaaaaaa', 0)
    INSERT [dbo].[danh_muc] ([id], [ma_danh_muc], [ten_danh_muc], [trang_thai_xoa]) VALUES (42, N'DM-A9N4', N'test add nhanh', 0)
    SET IDENTITY_INSERT [dbo].[danh_muc] OFF

    SET IDENTITY_INSERT [dbo].[danh_muc_chi_tiet] ON

    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (1, 6, N'DMCT-T2E4', N'Lẩu Thái chua cay truyền thống', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (2, 6, N'DMCT-B4S3', N'Lẩu Thái dừa', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (3, 6, N'DMCT-W6A4', N'Lẩu Thái cay đặc biệt', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (4, 6, N'DMCT-F0H4', N'Lẩu Thái cà chua', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (5, 6, N'DMCT-K2W3', N'Lẩu Thái Bòa', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (6, 2, N'DMCT-B8K9', N'Sinh tố', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (7, 2, N'DMCT-S8U3', N'Trà', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (8, 2, N'DMCT-O5R8', N'Nước ép', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (9, 2, N'DMCT-N7W7', N'Bia', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (10, 2, N'DMCT-Q6H7', N'Rượu', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (11, 2, N'DMCT-F9D1', N'Nước ngọt', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (12, 3, N'DMCT-D9H4', N'Rau', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (13, 3, N'DMCT-E1D1', N'Thịt', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (14, 3, N'DMCT-Q1N2', N'Hải sản', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (15, 3, N'DMCT-Z2J2', N'Nấm', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (16, 3, N'DMCT-Y1J6', N'Viên thả lẩu', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (17, 1, N'DMCT-L0Q1', N'Mặc định', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (18, 4, N'DMCT-W4B2', N'Mặc định', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (19, 5, N'DMCT-V9D0', N'Đồ ăn kèm', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (26, 5, N'DMCT-G7F3', N'hi', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (27, 5, N'DMCT-P8V6', N'hifafa', 0)
    INSERT [dbo].[danh_muc_chi_tiet] ([id], [id_danh_muc], [ma_danh_muc_chi_tiet], [ten_danh_muc_chi_tiet], [trang_thai_xoa]) VALUES (28, 6, N'DMCT-K6E6', N'Lẩu khỉ dê già', 0)
    SET IDENTITY_INSERT [dbo].[danh_muc_chi_tiet] OFF

    SET IDENTITY_INSERT [dbo].[danh_muc_dinh_luong] ON

    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (1, 2, 1)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (2, 2, 2)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (3, 2, 3)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (4, 3, 4)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (5, 3, 5)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (6, 3, 6)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (7, 2, 7)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (8, 2, 8)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (9, 3, 9)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (10, 4, 10)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (29, 37, 4)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (30, 37, 5)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (31, 37, 6)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (32, 37, 8)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (33, 37, 12)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (34, 37, 13)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (35, 37, 14)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (36, 3, 24)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (37, 3, 25)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (38, 3, 26)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (39, 5, 24)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (40, 5, 25)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (41, 5, 26)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (42, 3, 27)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (43, 3, 28)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (44, 3, 29)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (45, 5, 27)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (46, 5, 28)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (47, 5, 29)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (48, 3, 30)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (49, 3, 31)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (50, 3, 32)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (51, 5, 30)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (52, 5, 31)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (53, 5, 32)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (54, 2, 33)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (55, 2, 34)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (56, 5, 33)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (57, 5, 34)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (62, 38, 1)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (63, 38, 2)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (64, 38, 8)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (65, 38, 24)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (66, 38, 25)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (67, 38, 26)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (86, 2, 44)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (87, 2, 45)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (88, 38, 44)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (89, 38, 45)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (90, 39, 7)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (94, 40, 30)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (95, 40, 31)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (96, 40, 32)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (97, 6, 46)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (98, 41, 46)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (99, 42, 18)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (100, 42, 19)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (101, 42, 20)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (102, 42, 33)
    INSERT [dbo].[danh_muc_dinh_luong] ([id], [id_danh_muc], [id_dinh_luong]) VALUES (103, 42, 34)
    SET IDENTITY_INSERT [dbo].[danh_muc_dinh_luong] OFF

    SET IDENTITY_INSERT [dbo].[dat_ban] ON

    INSERT [dbo].[dat_ban] ([id], [id_khach_hang], [id_ban_an], [ma_dat_ban], [thoi_gian_dat], [so_nguoi_toi_da], [ghi_chu], [ngay_tao], [ngay_cap_nhat], [hinh_thuc_dat], [trang_thai]) VALUES (1, 1, 1, N'DB-L9J5', CAST(N'2026-02-01T18:00:00.0000000' AS DateTime2), 4, N'Khách đặt trước buổi tối', CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), 2, NULL)
    INSERT [dbo].[dat_ban] ([id], [id_khach_hang], [id_ban_an], [ma_dat_ban], [thoi_gian_dat], [so_nguoi_toi_da], [ghi_chu], [ngay_tao], [ngay_cap_nhat], [hinh_thuc_dat], [trang_thai]) VALUES (2, 2, 7, N'DB-K3N6', CAST(N'2026-02-01T19:00:00.0000000' AS DateTime2), 6, N'Khách đi gia đình', CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), 2, NULL)
    INSERT [dbo].[dat_ban] ([id], [id_khach_hang], [id_ban_an], [ma_dat_ban], [thoi_gian_dat], [so_nguoi_toi_da], [ghi_chu], [ngay_tao], [ngay_cap_nhat], [hinh_thuc_dat], [trang_thai]) VALUES (3, 3, 13, N'DB-D2H9', CAST(N'2026-02-02T18:30:00.0000000' AS DateTime2), 8, N'Sinh nhật nhỏ', CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), 2, NULL)
    INSERT [dbo].[dat_ban] ([id], [id_khach_hang], [id_ban_an], [ma_dat_ban], [thoi_gian_dat], [so_nguoi_toi_da], [ghi_chu], [ngay_tao], [ngay_cap_nhat], [hinh_thuc_dat], [trang_thai]) VALUES (4, 4, 16, N'DB-P8F2', CAST(N'2026-02-02T20:00:00.0000000' AS DateTime2), 16, N'Liên hoan công ty', CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), 2, NULL)
    INSERT [dbo].[dat_ban] ([id], [id_khach_hang], [id_ban_an], [ma_dat_ban], [thoi_gian_dat], [so_nguoi_toi_da], [ghi_chu], [ngay_tao], [ngay_cap_nhat], [hinh_thuc_dat], [trang_thai]) VALUES (5, 5, 20, N'DB-L2K9', CAST(N'2026-02-03T18:00:00.0000000' AS DateTime2), 6, N'Khách quen', CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), CAST(N'2026-01-29T17:05:06.6090867' AS DateTime2), 2, NULL)
    SET IDENTITY_INSERT [dbo].[dat_ban] OFF

    SET IDENTITY_INSERT [dbo].[dia_chi] ON

    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (1, 1, N'Số 12 Trần Duy Hưng', N'Hà Nội', N'Cầu Giấy', N'Trung Hòa', N'Địa chỉ nhà riêng', 1, 0, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (2, 2, N'Số 88 Nguyễn Trãi', N'Hà Nội', N'Thanh Xuân', N'Hạ Đình', N'Giao giờ hành chính', 1, 0, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (3, 3, N'Số 25 Lạch Tray', N'Hải Phòng', N'Ngô Quyền', N'Lạch Tray', N'Nhà mặt đường', 1, 0, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (4, 4, N'Số 9 Lý Thái Tổ', N'Bắc Ninh', N'TP Bắc Ninh', N'Đại Phúc', N'Gần quảng trường', 1, 0, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (5, 5, N'Thôn Đông', N'Hà Nam', N'Duy Tiên', N'Hoàng Đông', N'Giao buổi tối', 1, 0, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (6, 6, N'hihihi', N'Thành phố Hà Nội', NULL, N'Phường Phúc Xá', NULL, NULL, 1, NULL, NULL)
    INSERT [dbo].[dia_chi] ([id], [id_khach_hang], [dia_chi_cu_the], [thanh_pho], [quan_huyen], [phuong_xa], [ghi_chu], [trang_thai], [mac_dinh], [ho_ten_nguoi_nhan], [sdt_nguoi_nhan]) VALUES (7, 6, N'hihihihi', N'Thành phố Hà Nội', NULL, N'Phường Trúc Bạch', NULL, NULL, 0, NULL, NULL)
    SET IDENTITY_INSERT [dbo].[dia_chi] OFF

    SET IDENTITY_INSERT [dbo].[dinh_luong] ON

    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (1, N'DL4143', CAST(330.00 AS Decimal(10, 2)), 2, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (2, N'DL71E5', CAST(500.00 AS Decimal(10, 2)), 2, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (3, N'DLCDE9', CAST(1.00 AS Decimal(10, 2)), 3, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (4, N'DLBEC5', CAST(200.00 AS Decimal(10, 2)), 1, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (5, N'DL91D6', CAST(300.00 AS Decimal(10, 2)), 1, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (6, N'DLB67C', CAST(500.00 AS Decimal(10, 2)), 1, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (7, N'DLE9AB', CAST(1.00 AS Decimal(10, 2)), 4, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (8, N'DL24E0', CAST(1.00 AS Decimal(10, 2)), 6, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (9, N'DL8075', CAST(1.00 AS Decimal(10, 2)), 7, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (10, N'DLC889', CAST(1.00 AS Decimal(10, 2)), 5, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (12, N'DL75AA', CAST(200.00 AS Decimal(10, 2)), 20, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (13, N'DL605B', CAST(300.00 AS Decimal(10, 2)), 20, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (14, N'DL8D40', CAST(500.00 AS Decimal(10, 2)), 20, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (15, N'DL1511', CAST(100.00 AS Decimal(10, 2)), 23, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (16, N'DL3CF7', CAST(200.00 AS Decimal(10, 2)), 23, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (17, N'DLBCE7', CAST(300.00 AS Decimal(10, 2)), 23, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (18, N'DL62DB', CAST(100.00 AS Decimal(10, 2)), 24, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (19, N'DLC4F8', CAST(200.00 AS Decimal(10, 2)), 24, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (20, N'DLAF45', CAST(300.00 AS Decimal(10, 2)), 24, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (21, N'DL8DCA', CAST(100.00 AS Decimal(10, 2)), 25, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (22, N'DL4C94', CAST(200.00 AS Decimal(10, 2)), 25, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (23, N'DL9EF0', CAST(300.00 AS Decimal(10, 2)), 25, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (24, N'DL81BE', CAST(100.00 AS Decimal(10, 2)), 26, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (25, N'DLFE1F', CAST(200.00 AS Decimal(10, 2)), 26, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (26, N'DL1DA4', CAST(300.00 AS Decimal(10, 2)), 26, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (27, N'DLF086', CAST(100.00 AS Decimal(10, 2)), 27, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (28, N'DL940E', CAST(200.00 AS Decimal(10, 2)), 27, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (29, N'DLABFA', CAST(300.00 AS Decimal(10, 2)), 27, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (30, N'DL3D07', CAST(100.00 AS Decimal(10, 2)), 28, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (31, N'DL03BE', CAST(200.00 AS Decimal(10, 2)), 28, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (32, N'DLB42E', CAST(300.00 AS Decimal(10, 2)), 28, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (33, N'DLC9F5', CAST(100.00 AS Decimal(10, 2)), 29, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (34, N'DLBE59', CAST(200.00 AS Decimal(10, 2)), 29, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (44, N'DLA5A7', CAST(330.00 AS Decimal(10, 2)), 30, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (45, N'DLA316', CAST(200.00 AS Decimal(10, 2)), 30, 0)
    INSERT [dbo].[dinh_luong] ([id], [ma_dinh_luong], [gia_tri], [id_don_vi], [trang_thai_xoa]) VALUES (46, N'DLE1E3', CAST(100.00 AS Decimal(10, 2)), 31, 0)
    SET IDENTITY_INSERT [dbo].[dinh_luong] OFF

    SET IDENTITY_INSERT [dbo].[don_vi] ON

    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (1, N'DV4FD5', N'Gram', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (2, N'DV7527', N'Ml', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (3, N'DV6356', N'L', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (4, N'DV401D', N'Chai', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (5, N'DV3B84', N'Phần', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (6, N'DV0306', N'Lon', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (7, N'DV703F', N'Đĩa', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (14, N'DVC5A9', N'TEST_TEN', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (15, N'DV7711', N'TEST_TEN', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (19, N'DV0057', N'HAHA', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (20, N'DVE24D', N'HAHA', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (21, N'DV0719', N'Test add', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (22, N'DVEA65', N'Test add', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (23, N'DV40CA', N'Test add', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (24, N'DV90F6', N'Test add ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (25, N'DVEC63', N'Test add ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (26, N'DVEB3D', N'Test add ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (27, N'DV7A25', N'Test add ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (28, N'DV8D3A', N'Test add ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (29, N'DV429B', N'TEST ADD ', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (30, N'DVC87B', N'Tes update', 0)
    INSERT [dbo].[don_vi] ([id], [ma_don_vi], [ten_don_vi], [trang_thai_xoa]) VALUES (31, N'DV18DB', N'test add bên danh mục', 0)
    SET IDENTITY_INSERT [dbo].[don_vi] OFF

    SET IDENTITY_INSERT [dbo].[giao_ca] ON

    INSERT [dbo].[giao_ca] ([id], [id_nhan_vien_giao], [id_nhan_vien_nhan], [id_ca_lam], [thoi_gian_giao], [ghi_chu]) VALUES (1, 1, 2, 1, CAST(N'2026-01-29T16:40:52.3615518' AS DateTime2), N'Giao ca sáng')
    INSERT [dbo].[giao_ca] ([id], [id_nhan_vien_giao], [id_nhan_vien_nhan], [id_ca_lam], [thoi_gian_giao], [ghi_chu]) VALUES (2, 2, 3, 2, CAST(N'2026-01-29T16:40:52.3635436' AS DateTime2), N'Giao ca chiều')
    INSERT [dbo].[giao_ca] ([id], [id_nhan_vien_giao], [id_nhan_vien_nhan], [id_ca_lam], [thoi_gian_giao], [ghi_chu]) VALUES (3, 3, 4, 3, CAST(N'2026-01-29T16:40:52.3635436' AS DateTime2), N'Giao ca tối')
    SET IDENTITY_INSERT [dbo].[giao_ca] OFF

    SET IDENTITY_INSERT [dbo].[hinh_anh] ON

    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (1, 1, N'nuoc_lau_thai_chua_cay.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (2, 2, N'nuoc_lau_thai_dua.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (3, 3, N'nuoc_lau_thai_cay_dac_biet.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (4, 4, N'nuoc_lau_thai_ca_chua.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (8, 8, N'nuoc_ep_cam.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (9, 9, N'bia_heineken.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (10, 10, N'ruou_vang_do.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (11, 11, N'coca_cola.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (12, 12, N'rau_cai_xanh.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (13, 13, N'thit_bo_my.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (14, 14, N'tom_su.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (15, 15, N'nam_kim_cham.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (16, 16, N'vien_tha_lau.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (17, 17, N'lau_thai_set_nho.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (18, 18, N'lau_thai_set_vua.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (19, 19, N'lau_thai_set_lon.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (20, 20, N'lau_thai_hai_san.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (21, 21, N'lau_thai_bo_my.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (22, 22, N'banh_flan.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (23, 23, N'kem_vani.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (24, 24, N'che_khuc_bach.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (25, 25, N'trai_cay_theo_mua.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (26, 26, N'rau_cau_dua.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (27, 27, N'kim_chi.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (28, 28, N'quay.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (29, 29, N'mi_tuoi.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (30, 30, N'bun_tuoi.jpg', 0, NULL)
    INSERT [dbo].[hinh_anh] ([id], [id_mon_an], [url], [trang_thai_xoa], [id_san_pham]) VALUES (31, 31, N'dau_phu.jpg', 0, NULL)
    SET IDENTITY_INSERT [dbo].[hinh_anh] OFF

    SET IDENTITY_INSERT [dbo].[hoa_don] ON

    INSERT [dbo].[hoa_don] ([id], [id_phieu_giam_gia], [id_khach_hang], [id_nhan_vien], [id_dat_ban], [ma_hoa_don], [loai_hoa_don], [thoi_gian_tao], [thoi_gian_thanh_toan], [tien_coc], [tien_thue], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_giao], [phi_van_chuyen], [so_tien_duoc_giam], [tong_tien], [trang_thai], [ghi_chu], [nguoi_tao], [ngay_cap_nhat], [id_ban_an], [thoi_gian_mo], [thoi_gian_dong]) VALUES (1, 1, 1, 1, 1, N'HD-U5X7', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, CAST(200000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), NULL, NULL, NULL, CAST(0.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(950000.00 AS Decimal(18, 2)), 0, N'Hóa đơn ăn tại chỗ', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, NULL, NULL)
    INSERT [dbo].[hoa_don] ([id], [id_phieu_giam_gia], [id_khach_hang], [id_nhan_vien], [id_dat_ban], [ma_hoa_don], [loai_hoa_don], [thoi_gian_tao], [thoi_gian_thanh_toan], [tien_coc], [tien_thue], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_giao], [phi_van_chuyen], [so_tien_duoc_giam], [tong_tien], [trang_thai], [ghi_chu], [nguoi_tao], [ngay_cap_nhat], [id_ban_an], [thoi_gian_mo], [thoi_gian_dong]) VALUES (2, NULL, 2, 2, 2, N'HD-X1P7', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), NULL, NULL, NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(720000.00 AS Decimal(18, 2)), 0, N'Khách đi gia đình', 2, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, NULL, NULL)
    INSERT [dbo].[hoa_don] ([id], [id_phieu_giam_gia], [id_khach_hang], [id_nhan_vien], [id_dat_ban], [ma_hoa_don], [loai_hoa_don], [thoi_gian_tao], [thoi_gian_thanh_toan], [tien_coc], [tien_thue], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_giao], [phi_van_chuyen], [so_tien_duoc_giam], [tong_tien], [trang_thai], [ghi_chu], [nguoi_tao], [ngay_cap_nhat], [id_ban_an], [thoi_gian_mo], [thoi_gian_dong]) VALUES (3, 2, 3, 1, 3, N'HD-P9J5', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), CAST(300000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), NULL, NULL, NULL, CAST(0.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), CAST(1200000.00 AS Decimal(18, 2)), 1, N'Đã thanh toán', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, NULL, NULL)
    INSERT [dbo].[hoa_don] ([id], [id_phieu_giam_gia], [id_khach_hang], [id_nhan_vien], [id_dat_ban], [ma_hoa_don], [loai_hoa_don], [thoi_gian_tao], [thoi_gian_thanh_toan], [tien_coc], [tien_thue], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_giao], [phi_van_chuyen], [so_tien_duoc_giam], [tong_tien], [trang_thai], [ghi_chu], [nguoi_tao], [ngay_cap_nhat], [id_ban_an], [thoi_gian_mo], [thoi_gian_dong]) VALUES (4, NULL, 4, 3, NULL, N'HD-U4B2', 2, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, CAST(0.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), N'Lãnh Minh Hiếu', N'0332654198', N'Hà Nội', CAST(30000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), CAST(450000.00 AS Decimal(18, 2)), 0, N'Giao hàng tận nơi', 3, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, NULL, NULL)
    INSERT [dbo].[hoa_don] ([id], [id_phieu_giam_gia], [id_khach_hang], [id_nhan_vien], [id_dat_ban], [ma_hoa_don], [loai_hoa_don], [thoi_gian_tao], [thoi_gian_thanh_toan], [tien_coc], [tien_thue], [ten_nguoi_nhan], [so_dien_thoai], [dia_chi_giao], [phi_van_chuyen], [so_tien_duoc_giam], [tong_tien], [trang_thai], [ghi_chu], [nguoi_tao], [ngay_cap_nhat], [id_ban_an], [thoi_gian_mo], [thoi_gian_dong]) VALUES (5, 1, 5, 2, 5, N'HD-U7O0', 1, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), CAST(500000.00 AS Decimal(18, 2)), CAST(0.00 AS Decimal(18, 2)), NULL, NULL, NULL, CAST(0.00 AS Decimal(18, 2)), CAST(80000.00 AS Decimal(18, 2)), CAST(1600000.00 AS Decimal(18, 2)), 1, N'Liên hoan công ty', 2, CAST(N'2026-01-29T17:06:29.5209489' AS DateTime2), NULL, NULL, NULL)
    SET IDENTITY_INSERT [dbo].[hoa_don] OFF

    SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] ON

    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (1, 1, 1, 1, CAST(120000.00 AS Decimal(18, 2)), CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (2, 2, 1, 1, CAST(120000.00 AS Decimal(18, 2)), CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (3, 3, 1, 1, CAST(120000.00 AS Decimal(18, 2)), CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (4, 4, 1, 1, CAST(120000.00 AS Decimal(18, 2)), CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (5, 5, 1, 1, CAST(120000.00 AS Decimal(18, 2)), CAST(120000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (6, 1, 2, 1, CAST(130000.00 AS Decimal(18, 2)), CAST(130000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (7, 2, 2, 1, CAST(130000.00 AS Decimal(18, 2)), CAST(130000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (8, 3, 2, 1, CAST(130000.00 AS Decimal(18, 2)), CAST(130000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (9, 4, 2, 1, CAST(130000.00 AS Decimal(18, 2)), CAST(130000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (10, 5, 2, 1, CAST(130000.00 AS Decimal(18, 2)), CAST(130000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (11, 1, 3, 1, CAST(140000.00 AS Decimal(18, 2)), CAST(140000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (12, 2, 3, 1, CAST(140000.00 AS Decimal(18, 2)), CAST(140000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (13, 3, 3, 1, CAST(140000.00 AS Decimal(18, 2)), CAST(140000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (14, 4, 3, 1, CAST(140000.00 AS Decimal(18, 2)), CAST(140000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    INSERT [dbo].[hoa_don_chi_tiet] ([id], [id_hoa_don], [id_mon_an_chi_tiet], [so_luong], [don_gia], [thanh_tien], [ngay_tao], [ngay_cap_nhat], [trang_thai], [id_san_pham]) VALUES (15, 5, 3, 1, CAST(140000.00 AS Decimal(18, 2)), CAST(140000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), CAST(N'2026-01-29T18:06:28.3803111' AS DateTime2), 1, NULL)
    SET IDENTITY_INSERT [dbo].[hoa_don_chi_tiet] OFF

    SET IDENTITY_INSERT [dbo].[khach_hang] ON

    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (1, N'KH-O5X7', N'123', N'Nguyễn Văn A', N'Hà Nội', N'0901111111', N'lanhmh868@gmail.com', CAST(N'1998-05-12' AS Date), 1, 1, NULL)
    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (2, N'KH-M0D7', N'123', N'Trần Thị B', N'Hà Nội', N'0902222222', N'b@gmail.com', CAST(N'1999-08-20' AS Date), 0, 1, NULL)
    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (3, N'KH-G2M8', N'123', N'Lê Văn C', N'Hải Phòng', N'0903333333', N'c@gmail.com', CAST(N'2000-01-15' AS Date), 1, 1, NULL)
    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (4, N'KH-D4P2', N'123', N'Phạm Thị D', N'Bắc Ninh', N'0904444444', N'd@gmail.com', CAST(N'1997-11-02' AS Date), 0, 1, NULL)
    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (5, N'KH-S2A2', N'123', N'Hoàng Văn E', N'Hà Nam', N'0905555555', N'e@gmail.com', CAST(N'2001-03-30' AS Date), 1, 1, NULL)
    INSERT [dbo].[khach_hang] ([id], [ma_khach_hang], [mat_khau], [ten_khach_hang], [dia_chi], [so_dien_thoai], [email], [ngay_sinh], [gioi_tinh], [trang_thai], [anh_dai_dien]) VALUES (6, N'KH-E6I3', N'$2a$10$JcqSSACnxJyf2dsZSW9t1OhmuJGddBm0rNO.eo5lt3ySm0XLf9Sby', N'Lãnh Minh Hiếu', NULL, N'0909090909', N'hieu2k4boy@gmail.com', CAST(N'2011-01-10' AS Date), 0, 1, NULL)
    SET IDENTITY_INSERT [dbo].[khach_hang] OFF

    SET IDENTITY_INSERT [dbo].[khach_hang_giam_gia] ON

    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (1, 1, 1, CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), N'Nguyễn Văn A', N'0901111111', N'a@gmail.com', CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (2, 2, 2, CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), N'Trần Thị B', N'0902222222', N'b@gmail.com', CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (4, 4, 4, CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), N'Phạm Thị D', N'0904444444', N'd@gmail.com', CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (5, 5, 5, CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), N'Hoàng Văn E', N'0905555555', N'e@gmail.com', CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), CAST(N'2026-01-29T16:51:40.3946360' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (6, 1, 7, NULL, NULL, NULL, NULL, CAST(N'2026-02-05T16:14:48.2734658' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (7, 2, 7, NULL, NULL, NULL, NULL, CAST(N'2026-02-05T16:14:48.2734658' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (8, 3, 7, NULL, NULL, NULL, NULL, CAST(N'2026-02-05T16:14:48.2734658' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (13, 1, 11, NULL, NULL, NULL, NULL, CAST(N'2026-02-05T20:15:35.9365036' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (14, 1, 12, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:29:46.8012248' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (15, 1, 13, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:34:47.1113729' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (16, 2, 13, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:34:47.1113729' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (17, 1, 14, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:40:18.7369425' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (18, 1, 15, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:43:11.3562995' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (19, 1, 16, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:46:52.5177440' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (20, 1, 17, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T19:59:50.2079387' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (21, 1, 18, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T20:02:23.5432620' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (22, 1, 19, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T20:07:54.0846558' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (23, 1, 20, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T20:18:41.7943012' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (24, 1, 21, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T20:21:05.9138627' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (25, 1, 22, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T20:30:45.7714932' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (26, 1, 23, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T21:19:53.9723257' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (27, 1, 24, NULL, NULL, NULL, NULL, CAST(N'2026-02-06T21:32:58.5135223' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (28, 1, 25, NULL, NULL, NULL, NULL, CAST(N'2026-03-04T10:06:58.2047380' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (29, 2, 25, NULL, NULL, NULL, NULL, CAST(N'2026-03-04T10:06:58.2047380' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (30, 1, 3, NULL, NULL, NULL, NULL, CAST(N'2026-03-04T11:24:13.9598519' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (31, 6, 3, NULL, NULL, NULL, NULL, CAST(N'2026-03-04T11:24:13.9598519' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (32, 1, 26, NULL, NULL, NULL, NULL, CAST(N'2026-03-07T18:29:32.1392447' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (33, 2, 26, NULL, NULL, NULL, NULL, CAST(N'2026-03-07T18:29:32.1392447' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (34, 1, 27, NULL, NULL, NULL, NULL, CAST(N'2026-03-07T18:37:46.9252606' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (35, 2, 27, NULL, NULL, NULL, NULL, CAST(N'2026-03-07T18:37:46.9252606' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (36, 6, 27, NULL, NULL, NULL, NULL, CAST(N'2026-03-07T18:37:46.9252606' AS DateTime2), NULL, NULL, NULL, NULL)
    INSERT [dbo].[khach_hang_giam_gia] ([id], [id_khach_hang], [id_phieu_giam_gia], [thoi_diem_giam_gia], [ten_khach_hang], [so_dien_thoai], [email], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa]) VALUES (37, 1, 28, NULL, NULL, NULL, NULL, CAST(N'2026-03-08T15:15:00.5912558' AS DateTime2), NULL, NULL, NULL, NULL)
    SET IDENTITY_INSERT [dbo].[khach_hang_giam_gia] OFF

    SET IDENTITY_INSERT [dbo].[khu_vuc] ON

    INSERT [dbo].[khu_vuc] ([id], [ma_khu_vuc], [ten_khu_vuc], [vi_tri], [mo_ta], [trang_thai]) VALUES (1, N'KV-L2R4', N'Tầng 1', N'Tầng 1', N'Khu vực tầng 1 – gần cửa ra vào', 1)
    INSERT [dbo].[khu_vuc] ([id], [ma_khu_vuc], [ten_khu_vuc], [vi_tri], [mo_ta], [trang_thai]) VALUES (2, N'KV-I7N0', N'Tầng 2', N'Tầng 2', N'Khu vực tầng 2 – không gian yên tĩnh', 1)
    SET IDENTITY_INSERT [dbo].[khu_vuc] OFF

    SET IDENTITY_INSERT [dbo].[lich_lam_viec] ON

    INSERT [dbo].[lich_lam_viec] ([id], [id_nhan_vien], [ngay], [vi_tri], [gio_bat_dau], [gio_ket_thuc], [ghi_chu], [trang_thai]) VALUES (1, 1, CAST(N'2026-01-30' AS Date), N'Quầy thu ngân', CAST(N'09:00:00' AS Time), CAST(N'17:00:00' AS Time), N'Ca sáng', 1)
    INSERT [dbo].[lich_lam_viec] ([id], [id_nhan_vien], [ngay], [vi_tri], [gio_bat_dau], [gio_ket_thuc], [ghi_chu], [trang_thai]) VALUES (2, 2, CAST(N'2026-01-30' AS Date), N'Phục vụ tầng 1', CAST(N'10:00:00' AS Time), CAST(N'18:00:00' AS Time), N'Phục vụ bàn lẩu Thái', 1)
    INSERT [dbo].[lich_lam_viec] ([id], [id_nhan_vien], [ngay], [vi_tri], [gio_bat_dau], [gio_ket_thuc], [ghi_chu], [trang_thai]) VALUES (3, 3, CAST(N'2026-01-30' AS Date), N'Bếp nóng', CAST(N'14:00:00' AS Time), CAST(N'22:00:00' AS Time), N'Chuẩn bị nước lẩu', 1)
    INSERT [dbo].[lich_lam_viec] ([id], [id_nhan_vien], [ngay], [vi_tri], [gio_bat_dau], [gio_ket_thuc], [ghi_chu], [trang_thai]) VALUES (4, 4, CAST(N'2026-01-30' AS Date), N'Phục vụ tầng 2', CAST(N'16:00:00' AS Time), CAST(N'23:00:00' AS Time), N'Ca tối đông khách', 1)
    SET IDENTITY_INSERT [dbo].[lich_lam_viec] OFF

    SET IDENTITY_INSERT [dbo].[lich_su_hoa_don] ON

    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (1, 1, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Tạo hóa đơn', 1, N'Hóa đơn ăn tại chỗ')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (2, 1, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Xác nhận đặt bàn', 1, N'Bàn đã được giữ')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (3, 2, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Tạo hóa đơn', 1, N'Khách đi gia đình')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (4, 3, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Tạo hóa đơn', 1, N'Liên hoan công ty')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (5, 3, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Thanh toán', 1, N'Khách đã thanh toán đủ')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (6, 4, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Tạo hóa đơn', 1, N'Hóa đơn giao hàng')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (7, 4, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Giao hàng', 1, N'Đơn đã giao thành công')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (8, 5, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Tạo hóa đơn', 1, N'Đặt bàn đông người')
    INSERT [dbo].[lich_su_hoa_don] ([id], [id_hoa_don], [ngay_tao], [ngay_cap_nhat], [thoi_gian], [hanh_dong], [trang_thai], [ghi_chu]) VALUES (9, 5, CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), CAST(N'2026-01-29T17:09:27.6134569' AS DateTime2), N'Thanh toán', 1, N'Thanh toán tiền cọc và còn lại')
    SET IDENTITY_INSERT [dbo].[lich_su_hoa_don] OFF

    SET IDENTITY_INSERT [dbo].[lich_su_thanh_toan] ON

    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (1, 1, N'0961330306', CAST(200000.00 AS Decimal(18, 2)), N'Tiền mặt', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán tiền cọc')
    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (2, 1, N'9210596344', CAST(750000.00 AS Decimal(18, 2)), N'Chuyển khoản', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán phần còn lại')
    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (3, 3, N'1341675762', CAST(1200000.00 AS Decimal(18, 2)), N'Chuyển khoản', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán đủ hóa đơn')
    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (4, 4, N'3409904439', CAST(450000.00 AS Decimal(18, 2)), N'Tiền mặt', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán khi nhận hàng')
    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (5, 5, N'2814576837', CAST(500000.00 AS Decimal(18, 2)), N'Tiền mặt', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán tiền cọc')
    INSERT [dbo].[lich_su_thanh_toan] ([id], [id_hoa_don], [ma_giao_dich], [so_tien], [phuong_thuc], [trang_thai], [thoi_gian], [ghi_chu]) VALUES (6, 5, N'1175668936', CAST(1100000.00 AS Decimal(18, 2)), N'Chuyển khoản', 1, CAST(N'2026-01-29T17:10:12.9279257' AS DateTime2), N'Thanh toán phần còn lại')
    SET IDENTITY_INSERT [dbo].[lich_su_thanh_toan] OFF

    SET IDENTITY_INSERT [dbo].[mon_an] ON

    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (1, 1, N'MA-O1C6', N'Lẩu Thái chua cay truyền thống', N'Chua cay vừa', N'lau_thai_1.jpg', CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (2, 2, N'MA-B8Q9', N'Lẩu Thái dừa', N'Béo nhẹ vị dừa', N'lau_thai_2.jpg', CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (3, 3, N'MA-X6R2', N'Lẩu Thái cay đặc biệt', N'Cay nồng đậm', N'lau_thai_3.jpg', CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (4, 4, N'MA-H7D7', N'Lẩu Thái cà chua', N'Chua dịu dễ ăn', N'lau_thai_4.jpg', CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), CAST(N'2026-01-29T17:54:38.0881060' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (8, 8, N'MA-C4C6', N'Nước ép cam', N'Nguyên chất', N'1770896220887_drink_6.jpg', CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), CAST(N'2026-02-12T18:37:00.9001164' AS DateTime2), 1, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (9, 9, N'MA-E0J2', N'Bia Heineken', N'Lon 330ml', N'drink_4.jpg', CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (10, 10, N'MA-J1O9', N'Rượu vang đỏ', N'Ly', N'drink_5.jpg', CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (11, 11, N'MA-R4V0', N'Coca Cola', N'Lon', N'drink_6.jpg', CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), CAST(N'2026-01-29T17:54:38.0991041' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (12, 12, N'MA-R4Z5', N'Rau cải xanh', N'Tươi', N'nhung_1.jpg', CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (13, 13, N'MA-N7K5', N'Thịt bò Mỹ', N'Mềm', N'nhung_2.jpg', CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (14, 14, N'MA-M3W6', N'Tôm sú', N'Tươi sống', N'nhung_3.jpg', CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (15, 15, N'MA-Z6K1', N'Nấm kim châm', N'Giòn', N'nhung_4.jpg', CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (16, 16, N'MA-X7P3', N'Viên thả lẩu', N'Tổng hợp', N'nhung_5.jpg', CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), CAST(N'2026-01-29T17:54:45.5500773' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (17, 17, N'MA-F3W2', N'Lẩu Thái set nhỏ', N'2 người', N'lau_set1.jpg', CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (18, 17, N'MA-I5B3', N'Lẩu Thái set vừa', N'3-4 người', N'lau_set2.jpg', CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (19, 17, N'MA-S1O4', N'Lẩu Thái set lớn', N'5-6 người', N'lau_set3.jpg', CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (20, 17, N'MA-W4R8', N'Lẩu Thái hải sản', N'Đặc biệt', N'lau_set4.jpg', CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (21, 17, N'MA-V2E0', N'Lẩu Thái bò Mỹ', N'Cao cấp', N'lau_set5.jpg', CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), CAST(N'2026-01-29T17:54:45.5530763' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (22, 18, N'MA-O2U8', N'Bánh flan', N'Mềm mịn', N'tm_1.jpg', CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (23, 18, N'MA-F8G7', N'Kem vani', N'Mát lạnh', N'tm_2.jpg', CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (24, 18, N'MA-J6H6', N'Chè khúc bạch', N'Ngọt nhẹ', N'tm_3.jpg', CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (25, 18, N'MA-V7G4', N'Trái cây theo mùa', N'Tươi', N'tm_4.jpg', CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (26, 18, N'MA-X9G5', N'Rau câu dừa', N'Thơm', N'tm_5.jpg', CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), CAST(N'2026-01-29T17:54:45.5540761' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (27, 19, N'MA-R3S3', N'Kim chi', N'Cay', N'ak_1.jpg', CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (28, 19, N'MA-S1S9', N'Quẩy', N'Giòn', N'ak_2.jpg', CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (29, 19, N'MA-M8A5', N'Mì tươi', N'Mềm', N'ak_3.jpg', CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (30, 19, N'MA-W0S1', N'Bún tươi', N'Trắng', N'ak_4.jpg', CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (31, 19, N'MA-J4J8', N'Đậu phụ', N'Béo', N'ak_5.jpg', CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), CAST(N'2026-01-29T17:54:45.5550784' AS DateTime2), 1, 1, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (55, 8, N'MA-L6F9', N'a', N'a', N'drink_6.jpg', CAST(N'2026-02-11T15:32:28.3744008' AS DateTime2), CAST(N'2026-02-11T20:20:18.3311167' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (56, 8, N'MA-T4P3', N'a', N'a', N'drink_3.jpg', CAST(N'2026-02-11T15:45:01.6374893' AS DateTime2), CAST(N'2026-02-11T15:45:01.6374893' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (57, 8, N'MA-P9V9', N'Test Update', N'Update thử', N'drink_4.jpg', CAST(N'2026-02-11T15:48:46.0933423' AS DateTime2), CAST(N'2026-02-11T20:08:32.5138465' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (58, 8, N'MA-K4H6', N'Lãnh Minh Hiếu ', N'Hiếu Test', N'drink_3.jpg', CAST(N'2026-02-11T20:43:17.0102385' AS DateTime2), CAST(N'2026-02-11T20:43:17.0102385' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (59, 19, N'MA-J3R5', N'aaaaaaaaa', N'aaaaaaaaa', N'drink_5.jpg', CAST(N'2026-02-11T20:56:51.3399330' AS DateTime2), CAST(N'2026-02-11T20:56:51.3399330' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (60, 9, N'MA-M3H9', N'aaaaaaaaaaaaaaaa', N'aaaaaaaaaaaaaaaaaaaaaaaaaaa', N'drink_3.jpg', CAST(N'2026-02-11T20:57:18.6125439' AS DateTime2), CAST(N'2026-02-11T20:57:18.6125439' AS DateTime2), 0, 0, 0)
    INSERT [dbo].[mon_an] ([id], [id_danh_muc_chi_tiet], [ma_mon_an], [ten_mon_an], [mo_ta], [hinh_anh], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (61, 9, N'MA-M8Z4', N'aaaaaaaaaaaa', N'aaa', N'drink_6.jpg', CAST(N'2026-02-11T21:07:31.2914938' AS DateTime2), CAST(N'2026-02-11T21:07:31.2914938' AS DateTime2), 0, 0, 0)
    SET IDENTITY_INSERT [dbo].[mon_an] OFF

    SET IDENTITY_INSERT [dbo].[mon_an_chi_tiet] ON

    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (1, 1, N'MACT-Z6S8', N'Phần nước lẩu', CAST(120000.00 AS Decimal(18, 2)), NULL, N'Nồi', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (2, 2, N'MACT-S2C5', N'Phần nước lẩu', CAST(130000.00 AS Decimal(18, 2)), NULL, N'Nồi', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (3, 3, N'MACT-L2I0', N'Phần nước lẩu', CAST(140000.00 AS Decimal(18, 2)), NULL, N'Nồi', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (4, 4, N'MACT-E8U0', N'Phần nước lẩu', CAST(125000.00 AS Decimal(18, 2)), NULL, N'Nồi', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (8, 8, N'MACT-T1F5', N'Ly nước ép', CAST(20000.00 AS Decimal(18, 2)), N'M', N'Chai', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-02-12T18:37:00.9041332' AS DateTime2), N'1', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (9, 9, N'MACT-L0J6', N'Lon bia', CAST(40000.00 AS Decimal(18, 2)), NULL, N'Lon', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (10, 10, N'MACT-O5I9', N'Ly rượu', CAST(60000.00 AS Decimal(18, 2)), NULL, N'Ly', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (11, 11, N'MACT-D3U0', N'Lon coca', CAST(20000.00 AS Decimal(18, 2)), NULL, N'Lon', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (12, 12, N'MACT-U8A4', N'Đĩa rau', CAST(25000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (13, 13, N'MACT-X2W6', N'Đĩa thịt', CAST(90000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (14, 14, N'MACT-G6D4', N'Đĩa tôm', CAST(95000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (15, 15, N'MACT-B7C1', N'Đĩa nấm', CAST(30000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (16, 16, N'MACT-P4T1', N'Đĩa viên thả', CAST(45000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (17, 17, N'MACT-H7J4', N'Set lẩu nhỏ', CAST(299000.00 AS Decimal(18, 2)), N'S', N'Set', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (18, 18, N'MACT-M9Y0', N'Set lẩu vừa', CAST(399000.00 AS Decimal(18, 2)), N'M', N'Set', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (19, 19, N'MACT-S5K9', N'Set lẩu lớn', CAST(499000.00 AS Decimal(18, 2)), N'L', N'Set', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (20, 20, N'MACT-N6L1', N'Set hải sản', CAST(599000.00 AS Decimal(18, 2)), N'XL', N'Set', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (21, 21, N'MACT-S0D2', N'Set bò Mỹ', CAST(649000.00 AS Decimal(18, 2)), N'XXL', N'Set', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (22, 22, N'MACT-D9S8', N'Phần bánh flan', CAST(20000.00 AS Decimal(18, 2)), NULL, N'Phần', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (23, 23, N'MACT-U6G6', N'Viên kem', CAST(25000.00 AS Decimal(18, 2)), NULL, N'Viên', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (24, 24, N'MACT-B9U2', N'Chén chè', CAST(30000.00 AS Decimal(18, 2)), NULL, N'Chén', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (25, 25, N'MACT-T5R8', N'Đĩa trái cây', CAST(35000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (26, 26, N'MACT-J7X9', N'Phần rau câu', CAST(25000.00 AS Decimal(18, 2)), NULL, N'Phần', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (27, 27, N'MACT-G7V1', N'Đĩa kim chi', CAST(20000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (28, 28, N'MACT-M5X0', N'Đĩa quẩy', CAST(15000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (29, 29, N'MACT-I9J6', N'Vắt mì', CAST(10000.00 AS Decimal(18, 2)), NULL, N'Vắt', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (30, 30, N'MACT-V9G3', N'Bún tươi', CAST(10000.00 AS Decimal(18, 2)), NULL, N'Phần', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (31, 31, N'MACT-J5I0', N'Đĩa đậu phụ', CAST(15000.00 AS Decimal(18, 2)), NULL, N'Đĩa', CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), CAST(N'2026-01-29T17:58:44.8872413' AS DateTime2), N'1', N'1', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (48, 55, N'MACT-K2Q2', N'aaaaaaaaaaaaaaaaaa', CAST(555.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T15:32:28.4771863' AS DateTime2), CAST(N'2026-02-11T20:20:18.3394486' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (49, 56, N'MACT-U2J3', N'a', CAST(333.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T15:45:01.6534881' AS DateTime2), CAST(N'2026-02-11T15:45:01.6534881' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (50, 57, N'MACT-G4K4', N'Test chi tieet', CAST(22222.00 AS Decimal(18, 2)), N'M', N'Ly', CAST(N'2026-02-11T15:48:46.1117473' AS DateTime2), CAST(N'2026-02-11T20:08:32.5300667' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (51, 58, N'MACT-T4W5', N'Hiếu Test', CAST(20000.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T20:43:17.1033102' AS DateTime2), CAST(N'2026-02-11T20:43:17.1033102' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (52, 58, N'MACT-X3S7', N'ggggg', CAST(55555.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T20:43:17.1162452' AS DateTime2), CAST(N'2026-02-11T20:43:17.1162452' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (53, 59, N'MACT-I6Z1', N'aaaaaaaa', CAST(33333333.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T20:56:51.3631109' AS DateTime2), CAST(N'2026-02-11T20:56:51.3631109' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (54, 60, N'MACT-U7C6', N'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', CAST(4444.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T20:57:18.6310858' AS DateTime2), CAST(N'2026-02-11T20:57:18.6310858' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (55, 61, N'MACT-L3Q7', N'aa', CAST(5555.00 AS Decimal(18, 2)), N'M', N'Lon', CAST(N'2026-02-11T21:07:31.3243618' AS DateTime2), CAST(N'2026-02-11T21:07:31.3243618' AS DateTime2), N'0', N'0', 0)
    INSERT [dbo].[mon_an_chi_tiet] ([id], [id_mon_an], [ma_mon_chi_tiet], [ten_mon_chi_tiet], [gia_ban], [size], [don_vi], [ngay_tao], [ngay_cap_nhat], [nguoi_tao], [nguoi_cap_nhat], [trang_thai_xoa]) VALUES (56, 61, N'MACT-K0O0', N'aaaa', CAST(6666.00 AS Decimal(18, 2)), N'L', N'Lon', CAST(N'2026-02-11T21:07:31.3487696' AS DateTime2), CAST(N'2026-02-11T21:07:31.3487696' AS DateTime2), N'0', N'0', 0)
    SET IDENTITY_INSERT [dbo].[mon_an_chi_tiet] OFF

    SET IDENTITY_INSERT [dbo].[nhan_vien] ON

    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (1, N'lanhminhhieu', N'123', N'NV-C2D5', N'Lãnh Minh Hiếu', N'hieu.png', CAST(N'2004-11-19' AS Date), N'0332654198', 1, N'hieulm24@gmail.com', N'024204001022', 1, 1, CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), 1, 1, 0, NULL, NULL, N'Hà Nội')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (2, N'nguyenthithuy', N'123', N'NV-D5A3', N'Nguyễn Thị Thùy', N'thuy.png', CAST(N'2003-04-22' AS Date), N'0353884935', 1, N'nguyenthithuy22042003@gmail.com', N'024204001022', 1, 1, CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), 1, 1, 0, NULL, NULL, N'Hà Nội')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (3, N'nguyenthidieuhien', N'123', N'NV-X9C5', N'Nguyễn Thị Diệu Hiền', N'hien.png', CAST(N'2004-11-19' AS Date), N'0364588611', 1, N'hn2317782@@gmail.com', N'024204001022', 0, 1, CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), 1, 1, 0, NULL, NULL, N'Hà Nội')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (4, N'doanhoanglong', N'123', N'NV-Z3Q3', N'Đoàn Hoàng Long', N'long.png', CAST(N'2004-07-15' AS Date), N'0384039806', 1, N'hoanglong15072004@gmail.com', N'024204001022', 0, 1, CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), CAST(N'2026-01-29T16:35:27.5349473' AS DateTime2), 1, 1, 0, NULL, NULL, N'Hà Nội')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (5, N'nv6666', N'$2a$10$4cY4rfpT.TJzARs369GHBO/9vpwEkVFhOBolTDOPwd44ZbzZCo5Xq', N'NV-U3E1', N'Lãnh Minh Hiếu', NULL, CAST(N'2026-02-06' AS Date), N'6666666666', 1, N'hieu2k4boy@gmail.com', N'666666666666', 0, 1, CAST(N'1970-01-01T16:04:37.6166667' AS DateTime2), NULL, NULL, NULL, 0, N'Thành phố Hà Nội', N'Phường Trúc Bạch', N'66666')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (6, N'nv2222', N'$2a$10$Ys5M8.LXNx5QYSNrucyPDe/fg14oHnfr7lHIfek0UNBe3Eh0cqwkO', N'NV-N1N5', N'haha', NULL, CAST(N'2026-02-04' AS Date), N'0999999999', 1, N'hieu2k4boy8@gmail.com', N'122222222222', 0, 1, CAST(N'1970-01-01T21:22:00.1500000' AS DateTime2), NULL, NULL, NULL, 0, N'Thành phố Hà Nội', N'Phường Phúc Xá', N'haha')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (7, N'nv3444', N'$2a$10$VpuBHLkAXIsOnbbHWL.xPej.7Fg1KnJJx4C5/D2d5QqZ0Iy94A/FO', N'NV-E3U3', N'aaaa', NULL, CAST(N'2026-02-02' AS Date), N'0987654321', 1, N'hieu2k4boy5@gmail.com', N'111222333444', 0, 1, CAST(N'1970-01-01T21:34:32.7733333' AS DateTime2), NULL, NULL, NULL, 0, N'Tỉnh Hà Giang', N'Phường Trần Phú', N'123')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (8, N'nv0102', N'$2a$10$Y5HVyV60UEX7obGtkhOZ9OlIKt8zkAjuJPUb5gPg8JV3OSM09HDIy', N'NV-D5V6', N'Lãnh Minh Hiếu', NULL, CAST(N'2003-09-21' AS Date), N'0909090909', 1, N'lanhmh868@gmail.com', N'064303000102', 0, 1, CAST(N'1970-01-01T21:12:16.4666667' AS DateTime2), NULL, NULL, NULL, 0, N'Tỉnh Nam Định', N'Xã Giao Phong', N'Xóm Lâm Hải')
    INSERT [dbo].[nhan_vien] ([id], [tai_khoan], [mat_khau], [ma_nhan_vien], [ten_nhan_vien], [anh_nhan_vien], [ngay_sinh], [so_dien_thoai], [gioi_tinh], [email], [can_cuoc_cong_dan], [chuc_vu], [trang_thai], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [tinh_thanh], [xa_phuong], [dia_chi_chi_tiet]) VALUES (9, N'nvE+11', N'$2a$10$dsGja6NYHVxLJ6raxl77euoGNRUE6ZUPPsZRh7jUyqWf3EPdUgB2O', N'NV-F6Q0', N'Lãnh Minh Hiếu', NULL, CAST(N'2000-11-11' AS Date), N'0909888888', 1, N'hieulm222@gmail.com', N'1.22222E+11', 0, 1, CAST(N'1970-01-01T20:51:59.3466667' AS DateTime2), NULL, NULL, NULL, 0, N'Bắc Giang', N'Đại sơn', N'123 hh')
    SET IDENTITY_INSERT [dbo].[nhan_vien] OFF

    SET IDENTITY_INSERT [dbo].[nuoc_lau] ON

    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (1, N'NL-S1O4', N'Lẩu thái chua cay', N'Vị chua cay cân bằng, dễ ăn', CAST(0.00 AS Decimal(18, 2)), 0, NULL)
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (2, N'NL-I5E9', N'Lẩu Thái dừa', N'Vị béo nhẹ từ nước dừa', CAST(10000.00 AS Decimal(18, 2)), 0, NULL)
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (3, N'NL-H6X2', N'Lẩu Thái cay đặc biệt', N'Cay nồng đậm, dành cho người ăn cay', CAST(15000.00 AS Decimal(18, 2)), 0, NULL)
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (4, N'NL-C6D0', N'Lẩu Thái cà chua', N'Vị chua dịu, thanh nhẹ', CAST(0.00 AS Decimal(18, 2)), 0, NULL)
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (5, N'NL-Y3U5', N'Lẩu Thái bò', N'Nước lẩu đậm vị bò', CAST(20000.00 AS Decimal(18, 2)), 0, NULL)
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (6, N'NL-G8S9', N'TEst ad nước lẩu', N'Chua', CAST(20000.00 AS Decimal(18, 2)), 0, N'185219d6-1769-4375-b6fd-aa16a6af1182_nuoclauthaihaisan.jpg')
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (7, N'NL-T0S4', N'Test update', N'11111', CAST(15000.00 AS Decimal(18, 2)), 0, N'255927ed-94ba-4ab4-86ea-61c9434cf5f2_lauthai1.jpg')
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (8, N'NL-V9S9', N'1', N'1', CAST(11111.00 AS Decimal(18, 2)), 0, N'34b08917-d526-4c73-abb0-14bf65ddabac_tom2.jpg')
    INSERT [dbo].[nuoc_lau] ([id], [ma_nuoc_lau], [ten_nuoc_lau], [mo_ta], [gia_phu_thu], [trang_thai_xoa], [hinh_anh]) VALUES (9, N'NL-Z3M5', N'ttt', N'tttt', CAST(20000.00 AS Decimal(18, 2)), 0, N'0571c613-cc43-4144-ac93-18c88eb33bb9_nuoclauthaihaisan.jpg')
    SET IDENTITY_INSERT [dbo].[nuoc_lau] OFF

    SET IDENTITY_INSERT [dbo].[permissions] ON

    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (1, N'XEM_DANH_MUC', N'Xem danh mục', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (2, N'THEM_DANH_MUC', N'Thêm danh mục', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (3, N'SUA_DANH_MUC', N'Sửa danh mục', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (4, N'XOA_DANH_MUC', N'Xóa danh mục', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (5, N'XEM_SAN_PHAM', N'Xem sản phẩm', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (6, N'THEM_SAN_PHAM', N'Thêm sản phẩm', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (7, N'SUA_SAN_PHAM', N'Sửa sản phẩm', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (8, N'XOA_SAN_PHAM', N'Xóa sản phẩm', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (9, N'XEM_THUOC_TINH', N'Xem thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (10, N'THEM_THUOC_TINH', N'Thêm thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (11, N'SUA_THUOC_TINH', N'Sửa thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (12, N'XOA_THUOC_TINH', N'Xóa thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (13, N'XEM_NUOC_LAU', N'Xem nước lẩu', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (14, N'THEM_NUOC_LAU', N'Thêm nước lẩu', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (15, N'SUA_NUOC_LAU', N'Sửa nước lẩu', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (16, N'XOA_NUOC_LAU', N'Xóa nước lẩu', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (17, N'XEM_NHAN_VIEN', N'Xem nhân viên', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (18, N'THEM_NHAN_VIEN', N'Thêm nhân viên', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (19, N'SUA_NHAN_VIEN', N'Sửa nhân viên', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (20, N'XOA_NHAN_VIEN', N'Xóa nhân viên', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (21, N'XEM_KHACH_HANG', N'Xem khách hàng', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (22, N'THEM_KHACH_HANG', N'Thêm khách hàng', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (23, N'SUA_KHACH_HANG', N'Sửa khách hàng', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (24, N'XOA_KHACH_HANG', N'Xóa khách hàng', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (25, N'XEM_BAN', N'Xem bàn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (26, N'THEM_BAN', N'Thêm bàn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (27, N'SUA_BAN', N'Sửa bàn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (28, N'XOA_BAN', N'Xóa bàn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (29, N'XEM_HOA_DON', N'Xem hóa đơn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (30, N'TAO_HOA_DON', N'Tạo hóa đơn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (31, N'SUA_HOA_DON', N'Sửa hóa đơn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (32, N'HUY_HOA_DON', N'Hủy hóa đơn', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (33, N'XEM_DIA_CHI_KHACH', N'Xem địa chỉ khách', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (34, N'THEM_DIA_CHI_KHACH', N'Thêm địa chỉ khách', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (35, N'DAT_DIA_CHI_MAC_DINH', N'Đặt địa chỉ mặc định', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (36, N'SUA_DIA_CHI_KHACH', N'Sửa địa chỉ khách', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (37, N'XEM_PHIEU_GIAM_GIA', N'Xem phiếu giảm giá', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (38, N'THEM_PHIEU_GIAM_GIA', N'Thêm phiếu giảm giá', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (39, N'SUA_PHIEU_GIAM_GIA', N'Sửa phiếu giảm giá', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (40, N'XOA_PHIEU_GIAM_GIA', N'Xóa phiếu giảm giá', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (41, N'XEM_KHU_VUC', N'Xem khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (42, N'THEM_KHU_VUC', N'Thêm khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (43, N'SUA_KHU_VUC', N'Sửa khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (44, N'XOA_KHU_VUC', N'Xóa khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (45, N'XEM_THUOC_TINH', N'Xem thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (46, N'THEM_THUOC_TINH', N'Thêm thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (47, N'SUA_THUOC_TINH', N'Sửa thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (48, N'XOA_THUOC_TINH', N'Xóa thuộc tính', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (49, N'XEM_KHU_VUC', N'Xem khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (50, N'THEM_KHU_VUC', N'Thêm khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (51, N'SUA_KHU_VUC', N'Sửa khu vực', 1)
    INSERT [dbo].[permissions] ([id], [ten_permission], [mo_ta], [trang_thai]) VALUES (52, N'XOA_KHU_VUC', N'Xóa khu vực', 1)
    SET IDENTITY_INSERT [dbo].[permissions] OFF

    SET IDENTITY_INSERT [dbo].[phieu_giam_gia] ON

    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (1, N'PGG-G3R4', N'Giảm 10% hóa đơn', 100, 1, 1, CAST(200000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T16:44:09.4506636' AS DateTime2), CAST(N'2026-01-29T16:44:09.4506636' AS DateTime2), 1, 1, 0, CAST(N'2026-02-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-28T00:00:00.0000000' AS DateTime2), 1)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (2, N'PGG-B2V3', N'Giảm 50K cho hóa đơn', 80, 1, 2, CAST(300000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-29T16:44:09.4626688' AS DateTime2), CAST(N'2026-01-29T16:44:09.4626688' AS DateTime2), 1, 1, 0, CAST(N'2026-02-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-28T00:00:00.0000000' AS DateTime2), 1)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (3, N'PGG-I3W3', N'Voucher riêng tư 15%', 2, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(25.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T16:44:09.4636718' AS DateTime2), CAST(N'2026-03-08T15:22:33.6740625' AS DateTime2), 1, 1, 0, CAST(N'2026-03-15T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-16T00:00:00.0000000' AS DateTime2), 1)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (4, N'PGG-A2F4', N'Voucher riêng tư 100K', 20, 2, 2, CAST(500000.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), NULL, CAST(N'2026-01-29T16:44:09.4636718' AS DateTime2), CAST(N'2026-03-08T15:23:08.1084521' AS DateTime2), 1, 1, 1, CAST(N'2026-02-01T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-31T00:00:00.0000000' AS DateTime2), 1)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (5, N'PGG-N0X8', N'Khuyến mãi khai trương 20%', 200, 1, 1, CAST(150000.00 AS Decimal(18, 2)), CAST(20.00 AS Decimal(18, 2)), CAST(70000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T16:44:09.4647819' AS DateTime2), CAST(N'2026-01-29T16:44:09.4647819' AS DateTime2), 1, 1, 0, CAST(N'2026-01-30T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-15T00:00:00.0000000' AS DateTime2), 1)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (6, N'PGG-D8D0', N'Khuyến mãi test', 50, 0, 0, CAST(500000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), CAST(N'2026-02-05T15:51:43.8817662' AS DateTime2), CAST(N'2026-02-05T15:51:43.8817662' AS DateTime2), 0, 0, 0, CAST(N'2026-02-05T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-10T23:59:59.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (7, N'PGG-W0L2', N'Phiếu riêng cho khách VIP', 5, 2, 1, CAST(200000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(N'2026-02-05T16:14:48.2734658' AS DateTime2), CAST(N'2026-02-05T16:14:48.2734658' AS DateTime2), 0, 0, 0, CAST(N'2026-02-05T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-20T23:59:59.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (11, N'PGG-I0I6', N'aaaa', 1, 2, 1, CAST(55555555555.00 AS Decimal(18, 2)), CAST(5555555555555.00 AS Decimal(18, 2)), CAST(55555.00 AS Decimal(18, 2)), CAST(N'2026-02-05T20:15:35.9365036' AS DateTime2), CAST(N'2026-02-05T20:15:35.9365036' AS DateTime2), 0, 0, 0, CAST(N'2026-02-04T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-07T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (12, N'PGG-W8G6', N'Lanh Minh Hieu', 1, 2, 1, CAST(1200000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(500000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:29:46.8012248' AS DateTime2), CAST(N'2026-02-06T19:29:46.8012248' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-15T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (13, N'PGG-B1H7', N'â', 2, 2, 1, CAST(3.00 AS Decimal(18, 2)), CAST(3.00 AS Decimal(18, 2)), CAST(33.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:34:47.1113729' AS DateTime2), CAST(N'2026-02-06T19:34:47.1113729' AS DateTime2), 0, 0, 0, CAST(N'2026-02-05T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (14, N'PGG-E3H7', N'Lãnh Minh Hiếu', 1, 2, 1, CAST(60.00 AS Decimal(18, 2)), CAST(20.00 AS Decimal(18, 2)), CAST(50.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:40:18.7369425' AS DateTime2), CAST(N'2026-02-06T19:40:18.7369425' AS DateTime2), 0, 0, 0, CAST(N'2026-02-07T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-19T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (15, N'PGG-N2T4', N'Lanh minh hieu', 1, 2, 1, CAST(22222.00 AS Decimal(18, 2)), CAST(2222.00 AS Decimal(18, 2)), CAST(222222.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:43:11.3562995' AS DateTime2), CAST(N'2026-02-06T19:43:11.3562995' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-14T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (16, N'PGG-M7V6', N'test', 1, 2, 1, CAST(333.00 AS Decimal(18, 2)), CAST(33.00 AS Decimal(18, 2)), CAST(333.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:46:52.5177440' AS DateTime2), CAST(N'2026-02-06T19:46:52.5177440' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-07T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (17, N'PGG-W9J2', N'a', 1, 2, 1, CAST(666.00 AS Decimal(18, 2)), CAST(567.00 AS Decimal(18, 2)), CAST(666.00 AS Decimal(18, 2)), CAST(N'2026-02-06T19:59:50.2079387' AS DateTime2), CAST(N'2026-02-06T19:59:50.2079387' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (18, N'PGG-M8Z4', N'aa', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(20000.00 AS Decimal(18, 2)), CAST(20000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T20:02:23.5432620' AS DateTime2), CAST(N'2026-02-06T20:02:23.5432620' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (19, N'PGG-O3M0', N'aa', 1, 2, 1, CAST(600000.00 AS Decimal(18, 2)), CAST(20.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T20:07:54.0846558' AS DateTime2), CAST(N'2026-02-06T20:07:54.0846558' AS DateTime2), 0, 0, 0, CAST(N'2026-02-14T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-15T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (20, N'PGG-Y3D8', N'Hiếu Test', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(20.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T20:18:41.7943012' AS DateTime2), CAST(N'2026-02-06T20:18:41.7943012' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-15T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (21, N'PGG-J1V8', N'Test', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(40000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T20:21:05.9138627' AS DateTime2), CAST(N'2026-02-06T20:21:05.9138627' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-14T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (22, N'PGG-Q4F7', N'Test', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(20.00 AS Decimal(18, 2)), CAST(100000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T20:30:45.7714932' AS DateTime2), CAST(N'2026-02-06T20:30:45.7714932' AS DateTime2), 0, 0, 0, CAST(N'2026-02-05T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-22T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (23, N'PGG-M2I6', N'Test', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T21:19:53.9723257' AS DateTime2), CAST(N'2026-02-06T21:19:53.9723257' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (24, N'PGG-D0W3', N'Lãnh Minh Hiếu 123', 1, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(50000.00 AS Decimal(18, 2)), CAST(N'2026-02-06T21:32:58.5135223' AS DateTime2), CAST(N'2026-02-06T21:32:58.5135223' AS DateTime2), 0, 0, 0, CAST(N'2026-02-06T00:00:00.0000000' AS DateTime2), CAST(N'2026-02-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (25, N'PGG-D7D1', N'TEst', 2, 2, 1, CAST(500000.00 AS Decimal(18, 2)), CAST(10.00 AS Decimal(18, 2)), CAST(20000.00 AS Decimal(18, 2)), CAST(N'2026-03-04T10:06:58.2047380' AS DateTime2), CAST(N'2026-03-04T10:06:58.2047380' AS DateTime2), 0, 0, 0, CAST(N'2026-03-04T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-05T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (26, N'PGG-X7E8', N'q', 2, 2, 1, CAST(111.00 AS Decimal(18, 2)), CAST(1.00 AS Decimal(18, 2)), CAST(1111.00 AS Decimal(18, 2)), CAST(N'2026-03-07T18:29:32.1392447' AS DateTime2), CAST(N'2026-03-07T18:29:32.1392447' AS DateTime2), 0, 0, 0, CAST(N'2026-03-07T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-21T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (27, N'PGG-Y8Y6', N'1111', 3, 2, 1, NULL, CAST(11.00 AS Decimal(18, 2)), CAST(11111.00 AS Decimal(18, 2)), CAST(N'2026-03-07T18:37:46.9252606' AS DateTime2), CAST(N'2026-03-07T18:37:46.9252606' AS DateTime2), 0, 0, 0, CAST(N'2026-03-07T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-08T00:00:00.0000000' AS DateTime2), NULL)
    INSERT [dbo].[phieu_giam_gia] ([id], [ma_giam_gia], [ten_giam_gia], [so_luong], [kieu_giam_gia], [phuong_thuc_giam_gia], [dieu_kien_su_dung], [gia_tri_giam], [giam_toi_da], [ngay_tao], [ngay_cap_nhat], [tao_boi], [cap_nhat_boi], [trang_thai_xoa], [ngay_bat_dau], [ngay_ket_thuc], [trang_thai]) VALUES (28, N'PGG-N0J4', N'test add', 1, 2, 1, CAST(111.00 AS Decimal(18, 2)), CAST(1.00 AS Decimal(18, 2)), CAST(11.00 AS Decimal(18, 2)), CAST(N'2026-03-08T15:15:00.5912558' AS DateTime2), CAST(N'2026-03-08T15:15:00.5912558' AS DateTime2), 0, 0, 0, CAST(N'2026-03-08T00:00:00.0000000' AS DateTime2), CAST(N'2026-03-15T00:00:00.0000000' AS DateTime2), NULL)
    SET IDENTITY_INSERT [dbo].[phieu_giam_gia] OFF

    SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] ON

    INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ma_phuong_thuc], [ten_phuong_thuc], [trang_thai], [ghi_chu]) VALUES (1, N'PTTT-Y6P5', N'Tiền mặt', 1, N'Thanh toán trực tiếp tại quầy')
    INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ma_phuong_thuc], [ten_phuong_thuc], [trang_thai], [ghi_chu]) VALUES (2, N'PTTT-W6M7', N'Chuyển khoản ngân hàng', 1, N'Chuyển khoản qua tài khoản ngân hàng')
    INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ma_phuong_thuc], [ten_phuong_thuc], [trang_thai], [ghi_chu]) VALUES (3, N'PTTT-F8K8', N'VNPay', 1, N'Thanh toán qua cổng VNPay')
    INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ma_phuong_thuc], [ten_phuong_thuc], [trang_thai], [ghi_chu]) VALUES (4, N'PTTT-A5U2', N'Momo', 1, N'Thanh toán ví điện tử Momo')
    INSERT [dbo].[phuong_thuc_thanh_toan] ([id], [ma_phuong_thuc], [ten_phuong_thuc], [trang_thai], [ghi_chu]) VALUES (5, N'PTTT-G1A1', N'ZaloPay', 1, N'Thanh toán ví điện tử ZaloPay')
    SET IDENTITY_INSERT [dbo].[phuong_thuc_thanh_toan] OFF

    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 1)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 2)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 3)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 4)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 5)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 6)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 7)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 8)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 9)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 10)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 11)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 12)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 13)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 14)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 15)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 16)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 17)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 18)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 19)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 20)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 21)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 22)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 23)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 24)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 25)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 26)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 27)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 28)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 29)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 30)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 31)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 32)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 33)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 34)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 35)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 36)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 37)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 38)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 39)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 40)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 41)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 42)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 43)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 44)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 49)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 50)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 51)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (1, 52)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 1)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 5)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 9)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 13)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 17)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 21)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 25)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 29)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 33)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 37)
    INSERT [dbo].[role_permissions] ([role_id], [permission_id]) VALUES (2, 41)

    SET IDENTITY_INSERT [dbo].[san_pham] ON

    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (1, 3, NULL, N'SP-DBUZ', N'Bò Mỹ', CAST(120000.00 AS Decimal(18, 2)), N'Bò nhập khẩu', 0, 1, 0, CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (2, 3, NULL, N'SP-XK9O', N'Ba chỉ bò', CAST(110000.00 AS Decimal(18, 2)), N'Ba chỉ mềm', 0, 1, 0, CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (3, 3, NULL, N'SP-CCI4', N'Mực tươi', CAST(95000.00 AS Decimal(18, 2)), N'Mực biển', 0, 1, 0, CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (4, 3, NULL, N'SP-U9MJ', N'Tôm sú', CAST(130000.00 AS Decimal(18, 2)), N'Tôm loại 1', 0, 1, 0, CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (5, 3, NULL, N'SP-4K4T', N'Viên thả lẩu', CAST(60000.00 AS Decimal(18, 2)), N'Combo viên', 0, 1, 0, CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), CAST(N'2026-02-13T10:12:29.0784961' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (6, 5, NULL, N'SP-TTT1', N'Mì trứng', CAST(20000.00 AS Decimal(18, 2)), N'Mì ăn lẩu', 0, 1, 0, CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (7, 5, NULL, N'SP-VVEW', N'Bún tươi', CAST(15000.00 AS Decimal(18, 2)), N'Bún ăn kèm', 0, 1, 0, CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (8, 5, NULL, N'SP-WDRV', N'Đậu hũ non', CAST(40000.00 AS Decimal(18, 2)), N'Đậu mềm', 0, 1, 0, CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (9, 5, NULL, N'SP-SWR1', N'Khoai môn', CAST(35000.00 AS Decimal(18, 2)), N'Khoai bùi', 0, 1, 0, CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (10, 5, NULL, N'SP-YEMW', N'Bắp Mỹ', CAST(30000.00 AS Decimal(18, 2)), N'Bắp ngọt', 0, 1, 0, CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), CAST(N'2026-02-13T10:12:41.7018211' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (11, 2, NULL, N'SP-MKDX', N'Coca Cola', CAST(20000.00 AS Decimal(18, 2)), N'Nước ngọt', 0, 1, 0, CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (12, 2, NULL, N'SP-CKG3', N'Pepsi', CAST(20000.00 AS Decimal(18, 2)), N'Nước ngọt', 0, 1, 0, CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (13, 2, NULL, N'SP-UR6O', N'7Up', CAST(20000.00 AS Decimal(18, 2)), N'Nước ngọt', 0, 1, 0, CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (14, 2, NULL, N'SP-NZMP', N'Trà đào', CAST(35000.00 AS Decimal(18, 2)), N'Trà trái cây', 0, 1, 0, CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (15, 2, NULL, N'SP-PWNJ', N'Bia Tiger', CAST(25000.00 AS Decimal(18, 2)), N'Bia lager', 0, 1, 0, CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), CAST(N'2026-02-13T10:12:51.3992623' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (16, 4, NULL, N'SP-CR81', N'Rau câu dừa', CAST(30000.00 AS Decimal(18, 2)), N'Tráng miệng mát', 0, 1, 0, CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (17, 4, NULL, N'SP-201E', N'Chè thái', CAST(35000.00 AS Decimal(18, 2)), N'Chè tổng hợp', 0, 1, 0, CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (18, 4, NULL, N'SP-HPR1', N'Kem vani', CAST(25000.00 AS Decimal(18, 2)), N'Kem lạnh', 0, 1, 0, CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (19, 4, NULL, N'SP-TJQB', N'Bánh flan', CAST(20000.00 AS Decimal(18, 2)), N'Flan caramel', 0, 1, 0, CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (20, 4, NULL, N'SP-Z2LB', N'Sữa chua', CAST(20000.00 AS Decimal(18, 2)), N'Sữa chua mát', 0, 1, 0, CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), CAST(N'2026-02-13T10:13:02.9951466' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (21, 1, 1, N'SP-ZU6R', N'Set lẩu nhỏ', CAST(299000.00 AS Decimal(18, 2)), N'2 người', 1, 1, 0, CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (22, 1, 2, N'SP-WQ5R', N'Set lẩu vừa', CAST(399000.00 AS Decimal(18, 2)), N'3-4 người', 1, 1, 0, CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (23, 1, 3, N'SP-80A4', N'Set lẩu lớn', CAST(499000.00 AS Decimal(18, 2)), N'4-5 người', 1, 1, 0, CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (24, 1, 4, N'SP-3LD1', N'Set gia đình', CAST(699000.00 AS Decimal(18, 2)), N'6 người', 1, 1, 0, CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (25, 1, 5, N'SP-79CV', N'Set đặc biệt', CAST(899000.00 AS Decimal(18, 2)), N'Cao cấp', 1, 1, 0, CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), CAST(N'2026-02-13T10:13:14.2149743' AS DateTime2), NULL, 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (26, 2, NULL, N'SP-6ATC', N'55', CAST(5.00 AS Decimal(18, 2)), N'55', 0, 1, 0, CAST(N'2026-02-23T21:26:22.8045589' AS DateTime2), CAST(N'2026-02-23T21:26:22.8045589' AS DateTime2), N'14da853a-0cf5-4597-8181-439752e7ac42_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (27, 2, NULL, N'SP-S6IO', N'aaaaaaaaaaa', CAST(555.00 AS Decimal(18, 2)), N'gggg', 0, 1, 0, CAST(N'2026-02-23T21:34:55.8301998' AS DateTime2), CAST(N'2026-02-23T21:34:55.8301998' AS DateTime2), N'17b1537d-e8af-418f-8dd2-e66970015a0a_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (28, 2, NULL, N'SP-BO9N', N'aaaaaaaaaaa', CAST(5555.00 AS Decimal(18, 2)), N'gggg', 0, 1, 0, CAST(N'2026-02-23T21:34:55.9162057' AS DateTime2), CAST(N'2026-02-23T21:34:55.9162057' AS DateTime2), N'17b1537d-e8af-418f-8dd2-e66970015a0a_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (29, 2, NULL, N'SP-LXE0', N'HÂHA', CAST(1.00 AS Decimal(18, 2)), N'', 0, 1, 0, CAST(N'2026-03-03T09:49:48.5530736' AS DateTime2), CAST(N'2026-03-03T09:49:48.5530736' AS DateTime2), N'4e78953a-3921-4ebb-8f63-e58cd79cd348_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (30, 2, NULL, N'SP-YC1E', N'HÂHA', CAST(2.00 AS Decimal(18, 2)), N'', 0, 1, 0, CAST(N'2026-03-03T09:49:48.6593382' AS DateTime2), CAST(N'2026-03-03T09:49:48.6593382' AS DateTime2), N'4e78953a-3921-4ebb-8f63-e58cd79cd348_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (31, 2, NULL, N'SP-4YHV', N'HAHAHAHA', CAST(20000.00 AS Decimal(18, 2)), N'HIHIHI', 0, 1, 0, CAST(N'2026-03-03T14:20:46.5744622' AS DateTime2), CAST(N'2026-03-03T14:20:46.5744622' AS DateTime2), N'1925a429-7dbe-4ef0-98e4-638efb4f0d01_sinhtobo.png', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (32, 2, NULL, N'SP-O4IP', N'hahaahhahahaha', CAST(22222.00 AS Decimal(18, 2)), N'hi', 0, 1, 0, CAST(N'2026-03-03T14:21:17.3942995' AS DateTime2), CAST(N'2026-03-03T14:21:17.3942995' AS DateTime2), N'860c83ce-c213-457f-8a72-e6749bf19bb2_nuocepcam.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (33, 2, NULL, N'SP-190B', N'hahaahhahahaha', CAST(222222.00 AS Decimal(18, 2)), N'hi', 0, 1, 0, CAST(N'2026-03-03T14:21:17.4231896' AS DateTime2), CAST(N'2026-03-03T14:21:17.4231896' AS DateTime2), N'860c83ce-c213-457f-8a72-e6749bf19bb2_nuocepcam.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (34, 2, NULL, N'SP-0FFE', N'111111', CAST(11111.00 AS Decimal(18, 2)), N'1', 0, 1, 0, CAST(N'2026-03-03T16:27:55.3468919' AS DateTime2), CAST(N'2026-03-03T16:53:16.3585050' AS DateTime2), N'dc34dff3-e07e-41c8-8503-6376eb96bd53_nuocepcam.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (35, 2, NULL, N'SP-9EU3', N'1', CAST(1.00 AS Decimal(18, 2)), N'1', 0, 1, 0, CAST(N'2026-03-03T16:56:05.2012097' AS DateTime2), CAST(N'2026-03-03T16:56:27.8759457' AS DateTime2), N'b5df49f3-1333-4379-8539-13de92936515_sinhtodau.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (38, 1, 2, N'SP-BFK8', N'HIHIHI', CAST(455000.00 AS Decimal(18, 2)), N'HIHIHI', 1, 1, 0, CAST(N'2026-03-04T09:12:54.5919106' AS DateTime2), CAST(N'2026-03-04T09:12:54.5919106' AS DateTime2), N'03508d05-4869-4903-8440-42253427873b_lauthai2.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (39, 1, 2, N'SP-VU0J', N'HIHIHI', CAST(547223.00 AS Decimal(18, 2)), N'hhhh', 1, 1, 0, CAST(N'2026-03-04T09:34:39.3869253' AS DateTime2), CAST(N'2026-03-04T09:34:39.3869253' AS DateTime2), N'37f3712e-07d3-4785-892b-8f83b9546b2e_lauthai2.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (40, 1, 2, N'SP-ZW7D', N'HIHIHI', CAST(1598006.00 AS Decimal(18, 2)), N'11111', 1, 1, 0, CAST(N'2026-03-04T09:35:52.7150420' AS DateTime2), CAST(N'2026-03-04T09:35:52.7150420' AS DateTime2), N'8ec93a07-f2ff-40d7-bfef-5e667721d39c_lauthai1.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (41, 1, 2, N'SP-D2XV', N'fffff', CAST(450558.00 AS Decimal(18, 2)), N'hhhh', 1, 1, 0, CAST(N'2026-03-04T11:54:53.2911677' AS DateTime2), CAST(N'2026-03-04T11:54:53.2921794' AS DateTime2), N'7a919c9d-1b19-43e8-8b14-c293c8d179b0_lauthai3.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (42, 2, NULL, N'SP-775J', N'Nước ép dứa test', CAST(20000.00 AS Decimal(18, 2)), N'Test', 0, 1, 0, CAST(N'2026-03-04T20:32:56.3461736' AS DateTime2), CAST(N'2026-03-04T20:32:56.3471701' AS DateTime2), N'4377217e-fd28-4328-8a76-662f11b18a3d_nuocepdua.jpg', 1)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (43, 2, NULL, N'SP-R7GT', N'Nước ép dứa test', CAST(28000.00 AS Decimal(18, 2)), N'Test', 0, 1, 0, CAST(N'2026-03-04T20:32:56.4513504' AS DateTime2), CAST(N'2026-03-04T20:33:23.9147337' AS DateTime2), N'4377217e-fd28-4328-8a76-662f11b18a3d_nuocepdua.jpg', 2)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (44, 1, 1, N'SP-NPD8', N'Lẩu thái chua và cay', CAST(499999.00 AS Decimal(18, 2)), N'Lẩu đẳng cấp', 1, 1, 0, CAST(N'2026-03-04T20:53:56.7861613' AS DateTime2), CAST(N'2026-03-04T20:53:56.7861613' AS DateTime2), N'e17a53ae-2107-43c5-9689-6c92c7889a37_lauthai1.jpg', NULL)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (45, 3, NULL, N'SP-4IZB', N'Test add nhanh', CAST(200000.00 AS Decimal(18, 2)), N'', 0, 1, 0, CAST(N'2026-03-05T10:16:44.3906041' AS DateTime2), CAST(N'2026-03-05T10:16:44.3906041' AS DateTime2), N'88bdc045-ad78-4076-ab49-96427c8203cd_nuocepdua.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (46, 3, NULL, N'SP-X61D', N'Test add nhanh', CAST(200000.00 AS Decimal(18, 2)), N'', 0, 1, 0, CAST(N'2026-03-05T10:16:44.6932496' AS DateTime2), CAST(N'2026-03-05T10:16:44.6932496' AS DateTime2), N'101eda8b-88ae-470f-a866-e51cc767e5e2_nuocepdua.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (47, 3, NULL, N'SP-NLKE', N'Test 2', CAST(20000.00 AS Decimal(18, 2)), N'HAHAHA', 0, 1, 0, CAST(N'2026-03-05T12:31:18.0263134' AS DateTime2), CAST(N'2026-03-05T12:31:18.0263134' AS DateTime2), N'6e2e6d8c-713c-43ca-966b-2438466afb51_nuocepcam.jpg', 5)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (48, 3, NULL, N'SP-6TVV', N'Test 2', CAST(25000.00 AS Decimal(18, 2)), N'HAHAHA', 0, 1, 0, CAST(N'2026-03-05T12:31:18.0809720' AS DateTime2), CAST(N'2026-03-05T12:31:18.0809720' AS DateTime2), N'6e2e6d8c-713c-43ca-966b-2438466afb51_nuocepcam.jpg', 6)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (49, 1, 2, N'SP-8KVS', N'Test update', CAST(511000.00 AS Decimal(18, 2)), N'HAHAAHA', 1, 1, 0, CAST(N'2026-03-05T15:57:23.7303936' AS DateTime2), CAST(N'2026-03-08T10:49:40.5021477' AS DateTime2), N'2e022e45-05c4-4828-a704-3558c36df54e_lauthai1.jpg', NULL)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (50, 3, NULL, N'SP-2L5N', N'ca vien', CAST(40000.00 AS Decimal(18, 2)), N'hihi', 0, 1, 0, CAST(N'2026-03-05T22:35:51.3830337' AS DateTime2), CAST(N'2026-03-05T22:35:51.3861520' AS DateTime2), N'f59caad3-7230-4a33-bcd5-213d2bcb48cd_cavien.jpg', 9)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (51, 1, 1, N'SP-G67M', N'test', CAST(925000.00 AS Decimal(18, 2)), N'hihi', 1, 1, 0, CAST(N'2026-03-05T22:36:08.9913185' AS DateTime2), CAST(N'2026-03-08T10:49:38.6396636' AS DateTime2), N'0f06d846-45ea-4ea8-86f6-56407cd52835_lauthai1.jpg', NULL)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (52, 3, NULL, N'SP-G32Y', N'hihi', CAST(444.00 AS Decimal(18, 2)), N'hihi', 0, 1, 0, CAST(N'2026-03-05T22:37:47.1268811' AS DateTime2), CAST(N'2026-03-05T22:37:47.1268811' AS DateTime2), N'e12e3c97-8b76-4a6d-8ba5-0b4ce98db27d_rau3.jpg', 9)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (53, 4, NULL, N'SP-UJSW', N'hihi', CAST(4444.00 AS Decimal(18, 2)), N'hihi', 0, 1, 0, CAST(N'2026-03-05T22:37:47.1555339' AS DateTime2), CAST(N'2026-03-06T14:22:02.4097838' AS DateTime2), N'7da459c4-78b3-4a97-8c38-6e0395cfe96a_dauphu.jpg', 10)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (54, 3, NULL, N'SP-Q93Z', N'ca vienn', CAST(29999.00 AS Decimal(18, 2)), N'mo ta', 0, 1, 0, CAST(N'2026-03-06T21:40:19.4061169' AS DateTime2), CAST(N'2026-03-06T21:40:19.4076726' AS DateTime2), N'46491f76-be97-4900-b5c7-14d75d0faae6_cavien.jpg', 9)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (55, 1, 1, N'SP-POON', N'hhihihi', CAST(455000.00 AS Decimal(18, 2)), N'mo ta', 1, 1, 0, CAST(N'2026-03-06T21:40:48.7246671' AS DateTime2), CAST(N'2026-03-06T21:41:25.9275261' AS DateTime2), N'0ad7ef70-92ab-4c70-a55e-dde97ab40027_lauthai1.jpg', NULL)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (56, 3, NULL, N'SP-OBV7', N'rau muong', CAST(15000.00 AS Decimal(18, 2)), N'rau', 0, 1, 0, CAST(N'2026-03-06T21:43:48.3978409' AS DateTime2), CAST(N'2026-03-06T21:43:48.3978409' AS DateTime2), N'adaee184-a389-4d88-a021-671c796561e8_rau3.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (57, 3, NULL, N'SP-81MJ', N'rau muong', CAST(1.00 AS Decimal(18, 2)), N'rau', 0, 1, 0, CAST(N'2026-03-06T21:43:48.4818101' AS DateTime2), CAST(N'2026-03-06T21:44:13.6188378' AS DateTime2), N'adaee184-a389-4d88-a021-671c796561e8_rau3.jpg', 9)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (58, 3, NULL, N'SP-O4OY', N'hú', CAST(3333.00 AS Decimal(18, 2)), N'uu', 0, 1, 0, CAST(N'2026-03-07T18:02:55.3779035' AS DateTime2), CAST(N'2026-03-07T18:02:55.3779035' AS DateTime2), N'96ccea78-a66a-4940-8b70-cb65fa0d5759_cavien.jpg', 9)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (59, 3, NULL, N'SP-2KQC', N'hú', CAST(66666.00 AS Decimal(18, 2)), N'uu', 0, 1, 0, CAST(N'2026-03-07T18:02:55.5245264' AS DateTime2), CAST(N'2026-03-07T18:02:55.5245264' AS DateTime2), N'96ccea78-a66a-4940-8b70-cb65fa0d5759_cavien.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (60, 3, NULL, N'SP-R4Q8', N'mực', CAST(1.00 AS Decimal(18, 2)), N'1', 0, 1, 0, CAST(N'2026-03-07T18:03:23.5311721' AS DateTime2), CAST(N'2026-03-07T18:03:23.5311721' AS DateTime2), N'6cb910b6-c7cd-40bf-8f02-c2b6392e1300_muc.jpg', 4)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (61, 5, NULL, N'SP-XUMV', N'tom', CAST(20000.00 AS Decimal(18, 2)), N'tets', 0, 1, 0, CAST(N'2026-03-07T18:42:27.6758456' AS DateTime2), CAST(N'2026-03-08T12:17:19.2589492' AS DateTime2), N'09cc083f-eb48-47e2-97de-347b443c02e9_tom2.jpg', 24)
    INSERT [dbo].[san_pham] ([id], [id_danh_muc], [id_nuoc_lau], [ma_san_pham], [ten_san_pham], [gia], [mo_ta], [la_set], [trang_thai], [trang_thai_xoa], [ngay_tao], [ngay_cap_nhat], [hinh_anh], [id_dinh_luong]) VALUES (62, 1, 5, N'SP-LDL0', N'tets', CAST(336667.00 AS Decimal(18, 2)), N'hi', 1, 1, 0, CAST(N'2026-03-07T18:42:50.3104652' AS DateTime2), CAST(N'2026-03-08T10:49:36.8665203' AS DateTime2), N'8c767d06-9f1b-4dde-ab96-bf5bdc9402be_lauthai2.jpg', 1)
    SET IDENTITY_INSERT [dbo].[san_pham] OFF

    SET IDENTITY_INSERT [dbo].[set_lau] ON

    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (1, 1, N'SL-B3G4', N'Set nhỏ', CAST(299000.00 AS Decimal(18, 2)), N'Phù hợp 2 người ăn', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (2, 1, N'SL-Z5H0', N'Set vừa', CAST(399000.00 AS Decimal(18, 2)), N'Phù hợp 3–4 người ăn', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (3, 1, N'SL-V7R1', N'Set lớn', CAST(549000.00 AS Decimal(18, 2)), N'Phù hợp 5–6 người ăn', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (4, 2, N'SL-C7S5', N'Set nhỏ', CAST(319000.00 AS Decimal(18, 2)), N'Vị béo nhẹ, 2 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (5, 2, N'SL-L4S1', N'Set vừa', CAST(419000.00 AS Decimal(18, 2)), N'Vị dừa đậm, 3–4 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (6, 2, N'SL-L3B0', N'Set lớn', CAST(569000.00 AS Decimal(18, 2)), N'Vị dừa béo, 5–6 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (7, 3, N'SL-C9K7', N'Set nhỏ', CAST(329000.00 AS Decimal(18, 2)), N'Cay nồng, 2 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (8, 3, N'SL-W6E8', N'Set vừa', CAST(429000.00 AS Decimal(18, 2)), N'Cay mạnh, 3–4 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (9, 3, N'SL-I8H5', N'Set lớn', CAST(589000.00 AS Decimal(18, 2)), N'Cay đặc biệt, 5–6 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (10, 4, N'SL-N2A9', N'Set nhỏ', CAST(289000.00 AS Decimal(18, 2)), N'Chua dịu, dễ ăn', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (11, 4, N'SL-M6O3', N'Set vừa', CAST(379000.00 AS Decimal(18, 2)), N'Phù hợp gia đình nhỏ', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (12, 4, N'SL-G5C7', N'Set lớn', CAST(529000.00 AS Decimal(18, 2)), N'Ăn đông người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (13, 5, N'SL-D6F1', N'Set nhỏ', CAST(349000.00 AS Decimal(18, 2)), N'Đậm vị bò, 2 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (14, 5, N'SL-F9S5', N'Set vừa', CAST(459000.00 AS Decimal(18, 2)), N'Bò nhiều, 3–4 người', 0)
    INSERT [dbo].[set_lau] ([id], [id_nuoc_lau], [ma_set_lau], [ten_set], [gia_set], [mo_ta], [trang_thai_xoa]) VALUES (15, 5, N'SL-F3A0', N'Set lớn', CAST(629000.00 AS Decimal(18, 2)), N'Bò cao cấp, 5–6 người', 0)
    SET IDENTITY_INSERT [dbo].[set_lau] OFF

    SET IDENTITY_INSERT [dbo].[set_lau_chi_tiet] ON

    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (1, 1, 12, N'Rau ăn kèm cho set nhỏ', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (2, 1, 13, N'Thịt bò chính cho set nhỏ', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (3, 1, 15, N'Nấm ăn kèm', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (4, 1, 19, N'Mì tinh cho set nhỏ', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (5, 2, 12, N'Rau ăn kèm cho set vừa', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (6, 2, 13, N'Thịt bò chính', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (7, 2, 14, N'Hải sản tôm sú', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (8, 2, 15, N'Nấm tổng hợp', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (9, 2, 19, N'Mì tinh cho set vừa', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (10, 3, 12, N'Rau nhiều cho set lớn', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (11, 3, 13, N'Thịt bò Mỹ phần lớn', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (12, 3, 14, N'Tôm sú nhiều', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (13, 3, 16, N'Viên thả lẩu tổng hợp', 0)
    INSERT [dbo].[set_lau_chi_tiet] ([id], [id_set_lau], [id_mon_an_chi_tiet], [mo_ta], [trang_thai_xoa]) VALUES (14, 3, 19, N'Mì tinh nhiều phần', 0)
    SET IDENTITY_INSERT [dbo].[set_lau_chi_tiet] OFF

    SET IDENTITY_INSERT [dbo].[set_thanh_phan] ON

    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (1, 21, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (2, 21, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (3, 21, 5, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (4, 21, 6, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (5, 21, 7, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (6, 22, 1, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (7, 22, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (8, 22, 4, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (9, 22, 5, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (10, 22, 7, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (11, 23, 1, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (12, 23, 2, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (13, 23, 3, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (14, 23, 4, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (15, 23, 5, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (16, 24, 1, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (17, 24, 2, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (18, 24, 3, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (19, 24, 8, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (20, 24, 9, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (21, 25, 1, 4, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (22, 25, 2, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (23, 25, 3, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (24, 25, 4, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (25, 25, 5, 4, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (26, 38, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (27, 38, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (28, 38, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (29, 38, 4, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (30, 39, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (31, 39, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (32, 39, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (33, 39, 33, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (34, 39, 35, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (35, 40, 26, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (36, 40, 25, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (37, 40, 24, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (38, 40, 35, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (39, 41, 1, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (40, 41, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (41, 41, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (42, 41, 30, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (43, 41, 29, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (44, 41, 28, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (45, 44, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (46, 44, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (47, 44, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (48, 44, 4, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (49, 44, 43, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (50, 44, 42, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (51, 44, 35, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (52, 44, 34, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (53, 49, 43, 2, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (54, 49, 8, 2, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (55, 49, 7, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (56, 49, 6, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (57, 49, 43, 2, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (58, 49, 8, 2, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (59, 49, 7, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (60, 49, 6, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (61, 49, 43, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (62, 49, 8, 2, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (63, 49, 7, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (64, 49, 1, 3, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (65, 51, 1, 6, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (66, 51, 2, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (67, 51, 3, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (68, 51, 4, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (69, 51, 5, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (70, 51, 50, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (71, 51, 1, 6, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (72, 51, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (73, 51, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (74, 55, 1, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (75, 55, 2, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (76, 55, 3, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (77, 55, 4, 1, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (78, 55, 54, 5, 1)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (79, 55, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (80, 55, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (81, 55, 3, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (82, 55, 4, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (83, 62, 1, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (84, 62, 2, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (85, 62, 60, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (86, 62, 59, 1, 0)
    INSERT [dbo].[set_thanh_phan] ([id], [id_set], [id_san_pham_thanh_phan], [so_luong], [trang_thai_xoa]) VALUES (87, 62, 61, 1, 0)
    SET IDENTITY_INSERT [dbo].[set_thanh_phan] OFF

    SET IDENTITY_INSERT [dbo].[thanh_toan] ON

    INSERT [dbo].[thanh_toan] ([id], [id_hoa_don], [phuong_thuc_thanh_toan_id], [ma_thanh_toan], [tong_tien], [ngay_thanh_toan], [ngay_cap_nhat], [trang_thai]) VALUES (1, 1, 1, N'TT-1165105478', CAST(200000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), 1)
    INSERT [dbo].[thanh_toan] ([id], [id_hoa_don], [phuong_thuc_thanh_toan_id], [ma_thanh_toan], [tong_tien], [ngay_thanh_toan], [ngay_cap_nhat], [trang_thai]) VALUES (2, 3, 2, N'TT-3882155977', CAST(300000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), 1)
    INSERT [dbo].[thanh_toan] ([id], [id_hoa_don], [phuong_thuc_thanh_toan_id], [ma_thanh_toan], [tong_tien], [ngay_thanh_toan], [ngay_cap_nhat], [trang_thai]) VALUES (3, 4, 1, N'TT-1540599510', CAST(30000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), 1)
    INSERT [dbo].[thanh_toan] ([id], [id_hoa_don], [phuong_thuc_thanh_toan_id], [ma_thanh_toan], [tong_tien], [ngay_thanh_toan], [ngay_cap_nhat], [trang_thai]) VALUES (4, 5, 3, N'TT-1140383051', CAST(500000.00 AS Decimal(18, 2)), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), CAST(N'2026-01-29T18:20:23.5087615' AS DateTime2), 1)
    SET IDENTITY_INSERT [dbo].[thanh_toan] OFF

    SET ANSI_PADDING ON

/****** Object:  Index [uq_ten_danh_muc]    Script Date: 3/10/2026 9:55:54 AM ******/
ALTER TABLE [dbo].[danh_muc] ADD  CONSTRAINT [uq_ten_danh_muc] UNIQUE NONCLUSTERED
    (
    [ten_danh_muc] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]

    SET ANSI_PADDING ON

/****** Object:  Index [uk_dmct_ten_danh_muc]    Script Date: 3/10/2026 9:55:54 AM ******/
ALTER TABLE [dbo].[danh_muc_chi_tiet] ADD  CONSTRAINT [uk_dmct_ten_danh_muc] UNIQUE NONCLUSTERED
    (
    [ten_danh_muc_chi_tiet] ASC,
    [id_danh_muc] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]

/****** Object:  Index [IX_dia_chi_khach_hang_mac_dinh]    Script Date: 3/10/2026 9:55:54 AM ******/
CREATE NONCLUSTERED INDEX [IX_dia_chi_khach_hang_mac_dinh] ON [dbo].[dia_chi]
(
	[id_khach_hang] ASC,
	[mac_dinh] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, DROP_EXISTING = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]

SET ANSI_PADDING ON

/****** Object:  Index [UQ__dinh_luo__A8271F908D889973]    Script Date: 3/10/2026 9:55:54 AM ******/
ALTER TABLE [dbo].[dinh_luong] ADD UNIQUE NONCLUSTERED
    (
    [ma_dinh_luong] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]

    SET ANSI_PADDING ON

/****** Object:  Index [UQ__don_vi__8E4F8B7E9181DB96]    Script Date: 3/10/2026 9:55:54 AM ******/
ALTER TABLE [dbo].[don_vi] ADD UNIQUE NONCLUSTERED
    (
    [ma_don_vi] ASC
    )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, SORT_IN_TEMPDB = OFF, IGNORE_DUP_KEY = OFF, ONLINE = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]

ALTER TABLE [dbo].[ban_an] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[ban_an] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[ban_an] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[danh_gia] ADD  DEFAULT (sysdatetime()) FOR [ngay_danh_gia]

ALTER TABLE [dbo].[danh_gia] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[danh_muc] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[danh_muc_chi_tiet] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[dat_ban] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[dat_ban] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[dat_ban] ADD  CONSTRAINT [DF_dat_ban_hinh_thuc_dat]  DEFAULT ((2)) FOR [hinh_thuc_dat]

ALTER TABLE [dbo].[dia_chi] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[dia_chi] ADD  CONSTRAINT [DF_dia_chi_mac_dinh]  DEFAULT ((0)) FOR [mac_dinh]

ALTER TABLE [dbo].[dinh_luong] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[don_vi] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[giao_ca] ADD  DEFAULT (sysdatetime()) FOR [thoi_gian_giao]

ALTER TABLE [dbo].[hinh_anh] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[hinh_anh_nuoc_lau] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT (sysdatetime()) FOR [thoi_gian_tao]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [tien_coc]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [tien_thue]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [phi_van_chuyen]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [so_tien_duoc_giam]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT ((0)) FOR [trang_thai]

ALTER TABLE [dbo].[hoa_don] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[hoa_don_chi_tiet] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[khach_hang] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[khach_hang_giam_gia] ADD  DEFAULT (sysdatetime()) FOR [thoi_diem_giam_gia]

ALTER TABLE [dbo].[khach_hang_giam_gia] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[khach_hang_giam_gia] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[khach_hang_giam_gia] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[khu_vuc] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[lich_lam_viec] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[lich_su_hoa_don] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[lich_su_hoa_don] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[lich_su_hoa_don] ADD  DEFAULT (sysdatetime()) FOR [thoi_gian]

ALTER TABLE [dbo].[lich_su_hoa_don] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[lich_su_thanh_toan] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[lich_su_thanh_toan] ADD  DEFAULT (sysdatetime()) FOR [thoi_gian]

ALTER TABLE [dbo].[mon_an] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[mon_an] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[mon_an] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[mon_an_chi_tiet] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[mon_an_chi_tiet] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[mon_an_chi_tiet] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[nhan_vien] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[nuoc_lau] ADD  DEFAULT ((0)) FOR [gia_phu_thu]

ALTER TABLE [dbo].[nuoc_lau] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[permissions] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[phieu_giam_gia] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[phuong_thuc_thanh_toan] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[san_pham] ADD  DEFAULT ((0)) FOR [la_set]

ALTER TABLE [dbo].[san_pham] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[san_pham] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[san_pham] ADD  DEFAULT (sysdatetime()) FOR [ngay_tao]

ALTER TABLE [dbo].[san_pham] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[set_lau] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[set_lau_chi_tiet] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[set_thanh_phan] ADD  DEFAULT ((1)) FOR [so_luong]

ALTER TABLE [dbo].[set_thanh_phan] ADD  DEFAULT ((0)) FOR [trang_thai_xoa]

ALTER TABLE [dbo].[thanh_toan] ADD  DEFAULT (sysdatetime()) FOR [ngay_thanh_toan]

ALTER TABLE [dbo].[thanh_toan] ADD  DEFAULT (sysdatetime()) FOR [ngay_cap_nhat]

ALTER TABLE [dbo].[thanh_toan] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[users_token] ADD  DEFAULT ((1)) FOR [trang_thai]

ALTER TABLE [dbo].[ban_an]  WITH CHECK ADD  CONSTRAINT [fk_ban_an_khu_vuc] FOREIGN KEY([id_khu_vuc])
    REFERENCES [dbo].[khu_vuc] ([id])

ALTER TABLE [dbo].[ban_an] CHECK CONSTRAINT [fk_ban_an_khu_vuc]

ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [fk_danh_gia_hoa_don] FOREIGN KEY([id_hoa_don])
    REFERENCES [dbo].[hoa_don] ([id])

ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [fk_danh_gia_hoa_don]

ALTER TABLE [dbo].[danh_gia]  WITH CHECK ADD  CONSTRAINT [fk_danh_gia_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[danh_gia] CHECK CONSTRAINT [fk_danh_gia_khach_hang]

ALTER TABLE [dbo].[danh_muc_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_dmct_danh_muc] FOREIGN KEY([id_danh_muc])
    REFERENCES [dbo].[danh_muc] ([id])

ALTER TABLE [dbo].[danh_muc_chi_tiet] CHECK CONSTRAINT [fk_dmct_danh_muc]

ALTER TABLE [dbo].[danh_muc_dinh_luong]  WITH CHECK ADD  CONSTRAINT [fk_dmdl_danh_muc] FOREIGN KEY([id_danh_muc])
    REFERENCES [dbo].[danh_muc] ([id])

ALTER TABLE [dbo].[danh_muc_dinh_luong] CHECK CONSTRAINT [fk_dmdl_danh_muc]

ALTER TABLE [dbo].[danh_muc_dinh_luong]  WITH CHECK ADD  CONSTRAINT [fk_dmdl_dinh_luong] FOREIGN KEY([id_dinh_luong])
    REFERENCES [dbo].[dinh_luong] ([id])

ALTER TABLE [dbo].[danh_muc_dinh_luong] CHECK CONSTRAINT [fk_dmdl_dinh_luong]

ALTER TABLE [dbo].[dat_ban]  WITH CHECK ADD  CONSTRAINT [fk_dat_ban_ban_an] FOREIGN KEY([id_ban_an])
    REFERENCES [dbo].[ban_an] ([id])

ALTER TABLE [dbo].[dat_ban] CHECK CONSTRAINT [fk_dat_ban_ban_an]

ALTER TABLE [dbo].[dat_ban]  WITH CHECK ADD  CONSTRAINT [fk_dat_ban_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[dat_ban] CHECK CONSTRAINT [fk_dat_ban_khach_hang]

ALTER TABLE [dbo].[dia_chi]  WITH CHECK ADD  CONSTRAINT [fk_dia_chi_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[dia_chi] CHECK CONSTRAINT [fk_dia_chi_khach_hang]

ALTER TABLE [dbo].[dinh_luong]  WITH CHECK ADD  CONSTRAINT [fk_dinh_luong_don_vi] FOREIGN KEY([id_don_vi])
    REFERENCES [dbo].[don_vi] ([id])

ALTER TABLE [dbo].[dinh_luong] CHECK CONSTRAINT [fk_dinh_luong_don_vi]

ALTER TABLE [dbo].[giao_ca]  WITH CHECK ADD  CONSTRAINT [fk_giao_ca_ca_lam] FOREIGN KEY([id_ca_lam])
    REFERENCES [dbo].[ca_lam] ([id])

ALTER TABLE [dbo].[giao_ca] CHECK CONSTRAINT [fk_giao_ca_ca_lam]

ALTER TABLE [dbo].[giao_ca]  WITH CHECK ADD  CONSTRAINT [fk_giao_ca_nv_giao] FOREIGN KEY([id_nhan_vien_giao])
    REFERENCES [dbo].[nhan_vien] ([id])

ALTER TABLE [dbo].[giao_ca] CHECK CONSTRAINT [fk_giao_ca_nv_giao]

ALTER TABLE [dbo].[giao_ca]  WITH CHECK ADD  CONSTRAINT [fk_giao_ca_nv_nhan] FOREIGN KEY([id_nhan_vien_nhan])
    REFERENCES [dbo].[nhan_vien] ([id])

ALTER TABLE [dbo].[giao_ca] CHECK CONSTRAINT [fk_giao_ca_nv_nhan]

ALTER TABLE [dbo].[hinh_anh]  WITH CHECK ADD  CONSTRAINT [fk_hinh_anh_mon_an] FOREIGN KEY([id_mon_an])
    REFERENCES [dbo].[mon_an] ([id])

ALTER TABLE [dbo].[hinh_anh] CHECK CONSTRAINT [fk_hinh_anh_mon_an]

ALTER TABLE [dbo].[hinh_anh]  WITH CHECK ADD  CONSTRAINT [fk_hinh_anh_san_pham] FOREIGN KEY([id_san_pham])
    REFERENCES [dbo].[san_pham] ([id])

ALTER TABLE [dbo].[hinh_anh] CHECK CONSTRAINT [fk_hinh_anh_san_pham]

ALTER TABLE [dbo].[hinh_anh_nuoc_lau]  WITH CHECK ADD  CONSTRAINT [fk_hanl_nuoc_lau] FOREIGN KEY([id_nuoc_lau])
    REFERENCES [dbo].[nuoc_lau] ([id])

ALTER TABLE [dbo].[hinh_anh_nuoc_lau] CHECK CONSTRAINT [fk_hanl_nuoc_lau]

ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_hoa_don_ban] FOREIGN KEY([id_ban_an])
    REFERENCES [dbo].[ban_an] ([id])

ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [fk_hoa_don_ban]

ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_hoa_don_dat_ban] FOREIGN KEY([id_dat_ban])
    REFERENCES [dbo].[dat_ban] ([id])

ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [fk_hoa_don_dat_ban]

ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_hoa_don_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [fk_hoa_don_khach_hang]

ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_hoa_don_nhan_vien] FOREIGN KEY([id_nhan_vien])
    REFERENCES [dbo].[nhan_vien] ([id])

ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [fk_hoa_don_nhan_vien]

ALTER TABLE [dbo].[hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_hoa_don_phieu_giam_gia] FOREIGN KEY([id_phieu_giam_gia])
    REFERENCES [dbo].[phieu_giam_gia] ([id])

ALTER TABLE [dbo].[hoa_don] CHECK CONSTRAINT [fk_hoa_don_phieu_giam_gia]

ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD FOREIGN KEY([id_mon_an_chi_tiet])
    REFERENCES [dbo].[mon_an_chi_tiet] ([id])

ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_hdct_hoa_don] FOREIGN KEY([id_hoa_don])
    REFERENCES [dbo].[hoa_don] ([id])

ALTER TABLE [dbo].[hoa_don_chi_tiet] CHECK CONSTRAINT [fk_hdct_hoa_don]

ALTER TABLE [dbo].[hoa_don_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_hdct_san_pham] FOREIGN KEY([id_san_pham])
    REFERENCES [dbo].[san_pham] ([id])

ALTER TABLE [dbo].[hoa_don_chi_tiet] CHECK CONSTRAINT [fk_hdct_san_pham]

ALTER TABLE [dbo].[khach_hang_giam_gia]  WITH CHECK ADD  CONSTRAINT [fk_khgg_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[khach_hang_giam_gia] CHECK CONSTRAINT [fk_khgg_khach_hang]

ALTER TABLE [dbo].[khach_hang_giam_gia]  WITH CHECK ADD  CONSTRAINT [fk_khgg_phieu_giam_gia] FOREIGN KEY([id_phieu_giam_gia])
    REFERENCES [dbo].[phieu_giam_gia] ([id])

ALTER TABLE [dbo].[khach_hang_giam_gia] CHECK CONSTRAINT [fk_khgg_phieu_giam_gia]

ALTER TABLE [dbo].[lich_lam_viec]  WITH CHECK ADD  CONSTRAINT [fk_lich_lam_viec_nhan_vien] FOREIGN KEY([id_nhan_vien])
    REFERENCES [dbo].[nhan_vien] ([id])

ALTER TABLE [dbo].[lich_lam_viec] CHECK CONSTRAINT [fk_lich_lam_viec_nhan_vien]

ALTER TABLE [dbo].[lich_su_hoa_don]  WITH CHECK ADD  CONSTRAINT [fk_lshd_hoa_don] FOREIGN KEY([id_hoa_don])
    REFERENCES [dbo].[hoa_don] ([id])

ALTER TABLE [dbo].[lich_su_hoa_don] CHECK CONSTRAINT [fk_lshd_hoa_don]

ALTER TABLE [dbo].[lich_su_thanh_toan]  WITH CHECK ADD  CONSTRAINT [fk_lstt_hoa_don] FOREIGN KEY([id_hoa_don])
    REFERENCES [dbo].[hoa_don] ([id])

ALTER TABLE [dbo].[lich_su_thanh_toan] CHECK CONSTRAINT [fk_lstt_hoa_don]

ALTER TABLE [dbo].[mon_an]  WITH CHECK ADD  CONSTRAINT [fk_mon_an_dmct] FOREIGN KEY([id_danh_muc_chi_tiet])
    REFERENCES [dbo].[danh_muc_chi_tiet] ([id])

ALTER TABLE [dbo].[mon_an] CHECK CONSTRAINT [fk_mon_an_dmct]

ALTER TABLE [dbo].[mon_an_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_mact_mon_an] FOREIGN KEY([id_mon_an])
    REFERENCES [dbo].[mon_an] ([id])

ALTER TABLE [dbo].[mon_an_chi_tiet] CHECK CONSTRAINT [fk_mact_mon_an]

ALTER TABLE [dbo].[role_permissions]  WITH CHECK ADD FOREIGN KEY([permission_id])
    REFERENCES [dbo].[permissions] ([id])

ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [fk_san_pham_dinh_luong] FOREIGN KEY([id_dinh_luong])
    REFERENCES [dbo].[dinh_luong] ([id])

ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [fk_san_pham_dinh_luong]

ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [FK_sanpham_danhmuc] FOREIGN KEY([id_danh_muc])
    REFERENCES [dbo].[danh_muc] ([id])

ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [FK_sanpham_danhmuc]

ALTER TABLE [dbo].[san_pham]  WITH CHECK ADD  CONSTRAINT [FK_sanpham_nuoclau] FOREIGN KEY([id_nuoc_lau])
    REFERENCES [dbo].[nuoc_lau] ([id])

ALTER TABLE [dbo].[san_pham] CHECK CONSTRAINT [FK_sanpham_nuoclau]

ALTER TABLE [dbo].[set_lau]  WITH CHECK ADD  CONSTRAINT [fk_set_lau_nuoc_lau] FOREIGN KEY([id_nuoc_lau])
    REFERENCES [dbo].[nuoc_lau] ([id])

ALTER TABLE [dbo].[set_lau] CHECK CONSTRAINT [fk_set_lau_nuoc_lau]

ALTER TABLE [dbo].[set_lau_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_slct_mon_an_ct] FOREIGN KEY([id_mon_an_chi_tiet])
    REFERENCES [dbo].[mon_an_chi_tiet] ([id])

ALTER TABLE [dbo].[set_lau_chi_tiet] CHECK CONSTRAINT [fk_slct_mon_an_ct]

ALTER TABLE [dbo].[set_lau_chi_tiet]  WITH CHECK ADD  CONSTRAINT [fk_slct_set_lau] FOREIGN KEY([id_set_lau])
    REFERENCES [dbo].[set_lau] ([id])

ALTER TABLE [dbo].[set_lau_chi_tiet] CHECK CONSTRAINT [fk_slct_set_lau]

ALTER TABLE [dbo].[set_thanh_phan]  WITH CHECK ADD  CONSTRAINT [FK_setthanhphan_sanpham] FOREIGN KEY([id_san_pham_thanh_phan])
    REFERENCES [dbo].[san_pham] ([id])

ALTER TABLE [dbo].[set_thanh_phan] CHECK CONSTRAINT [FK_setthanhphan_sanpham]

ALTER TABLE [dbo].[set_thanh_phan]  WITH CHECK ADD  CONSTRAINT [FK_setthanhphan_set] FOREIGN KEY([id_set])
    REFERENCES [dbo].[san_pham] ([id])

ALTER TABLE [dbo].[set_thanh_phan] CHECK CONSTRAINT [FK_setthanhphan_set]

ALTER TABLE [dbo].[thanh_toan]  WITH CHECK ADD  CONSTRAINT [fk_tt_hoa_don] FOREIGN KEY([id_hoa_don])
    REFERENCES [dbo].[hoa_don] ([id])

ALTER TABLE [dbo].[thanh_toan] CHECK CONSTRAINT [fk_tt_hoa_don]

ALTER TABLE [dbo].[thanh_toan]  WITH CHECK ADD  CONSTRAINT [fk_tt_phuong_thuc] FOREIGN KEY([phuong_thuc_thanh_toan_id])
    REFERENCES [dbo].[phuong_thuc_thanh_toan] ([id])

ALTER TABLE [dbo].[thanh_toan] CHECK CONSTRAINT [fk_tt_phuong_thuc]

ALTER TABLE [dbo].[users_token]  WITH CHECK ADD  CONSTRAINT [fk_users_token_khach_hang] FOREIGN KEY([id_khach_hang])
    REFERENCES [dbo].[khach_hang] ([id])

ALTER TABLE [dbo].[users_token] CHECK CONSTRAINT [fk_users_token_khach_hang]

ALTER TABLE [dbo].[users_token]  WITH CHECK ADD  CONSTRAINT [FK_users_token_nhan_vien] FOREIGN KEY([id_nhan_vien])
    REFERENCES [dbo].[nhan_vien] ([id])

ALTER TABLE [dbo].[users_token] CHECK CONSTRAINT [FK_users_token_nhan_vien]

ALTER TABLE [dbo].[lich_lam_viec]  WITH CHECK ADD  CONSTRAINT [ck_gio_lam_viec] CHECK  (([gio_ket_thuc]>[gio_bat_dau]))

ALTER TABLE [dbo].[lich_lam_viec] CHECK CONSTRAINT [ck_gio_lam_viec]

ALTER DATABASE [DATABASE_DATN] SET  READ_WRITE


select * from hoa_don