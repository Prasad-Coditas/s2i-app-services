package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.ProductMasterDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.DistributionList;
import com.snap2buy.themobilebackend.model.ProductMaster;
import com.snap2buy.themobilebackend.model.UpcFacingDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sachin on 1/23/16.
 */
@Component(value = BeanMapper.BEAN_PRODUCT_MASTER_DAO)
@Scope("prototype")
public class ProductMasterDaoImpl implements ProductMasterDao {

    private static Logger LOGGER = LoggerFactory.getLogger(ProductMasterDaoImpl.class);

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "base-spring-ctx.xml");
        ProductMasterDaoImpl productMasterDaoImpl = (ProductMasterDaoImpl) context.getBean("ProductMasterDaoImpl");
        LOGGER.info("Checking logger");

        productMasterDaoImpl.getThumbnails("381519019340");

    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ProductMaster getUpcDetails(String upc) {
        LOGGER.info("---------------ProductMasterDaoImpl Starts getUpcDetails::upc={}",upc);
        String sql = "SELECT * FROM ProductMaster WHERE UPC = ?";

        Connection conn = null;
        ProductMaster productMaster = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, upc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                productMaster = new ProductMaster(rs.getString("UPC"),
                        rs.getString("MFG_NAME"),
                        rs.getString("BRAND_NAME"),
                        rs.getString("PRODUCT_TYPE"),
                        rs.getString("PRODUCT_SHORT_NAME"),
                        rs.getString("PRODUCT_LONG_NAME"),
                        rs.getString("attribute_1"),
                        rs.getString("attribute_2"),
                        rs.getString("attribute_3"),
                        rs.getString("attribute_4"),
                        rs.getString("attribute_5"),
                        rs.getString("why_buy_1"),
                        rs.getString("why_buy_2"),
                        rs.getString("why_buy_3"),
                        rs.getString("why_buy_4"),
                        rs.getString("romance_copy_1"),
                        rs.getString("romance_copy_2"),
                        rs.getString("romance_copy_3"),
                        rs.getString("romance_copy_4"),
                        rs.getString("height"),
                        rs.getString("width"),
                        rs.getString("depth"),
                        rs.getString("product_rating"),
                        rs.getString("PRODUCT_SUB_TYPE")
                );
                LOGGER.info("---------------ProductMasterDaoImpl - {}", productMaster.toString());

            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProductMasterDaoImpl Ends getUpcDetails----------------\n");

            return productMaster;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public File getThumbnails(String upc) {
        LOGGER.info("---------------ProductMasterDaoImpl Starts getThumbnails::upc={}",upc);
        String sql = "SELECT name, image FROM Thumbnails WHERE id = ?";
        String filePath = "filePath not found";
        File image = null;
        Connection conn = null;
        ProductMaster productMaster = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, upc);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString(1);
                LOGGER.info("---------------ProductMasterDaoImpl - getThumbnails image found {}", name);
                String dirPath = "/tmp/snap2buy/image/";
                File dir = new File(dirPath);
                if (!dir.getParentFile().exists()) {
                    dir.getParentFile().mkdirs();
                    dir.getParentFile().setReadable(true);
                    dir.getParentFile().setWritable(true);
                    dir.getParentFile().setExecutable(true);
                } else if (!dir.exists()) {
                    dir.setReadable(true);
                    dir.setWritable(true);
                    dir.setExecutable(true);
                }
                filePath = "/tmp/snap2buy/image/" + name;
                image = new File(filePath);
                FileOutputStream fos = new FileOutputStream(image);

                byte[] buffer = new byte[1];
                InputStream is = rs.getBinaryStream(2);
                while (is.read(buffer) > 0) {
                    fos.write(buffer);
                }
                fos.close();
            } else {
                String name = "default.jpg";
                LOGGER.info("---------------ProductMasterDaoImpl - getThumbnails {}", name );
                filePath = "/tmp/snap2buy/image/" + name;
                image = new File(filePath);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProductMasterDaoImpl Ends getThumbnails----------------\n");

            return image;

        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public void storeThumbnails(String imageFolderPath) {
        LOGGER.info("---------------ProductMasterDaoImpl Starts storeThumbnails imageFolderPath={}",imageFolderPath);

        String sql = "insert into Thumbnails(id, name, image) values (?, ?, ?)";
        File folder = new File(imageFolderPath);
        File[] listOfFiles = folder.listFiles();

        Connection conn = null;
        ProductMaster productMaster = null;
        FileInputStream fis = null;

        for (File file : listOfFiles) {
            try {
                String name = file.getName();
                LOGGER.info("---------------ProductMasterDaoImpl  image name ={}",name);
                fis = new FileInputStream(file);
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);

                PreparedStatement ps = conn.prepareStatement(sql);

                ps.setString(1, name.replace(".png", "").replace(".jpg", "").replace(".JPG", "").replace(".PNG", "").replace(".jpeg", "").replace(".JPEG", ""));
                ps.setString(2, name);
                ps.setBinaryStream(3, fis, (int) file.length());
                ps.executeUpdate();
                conn.commit();

                ps.close();
                fis.close();

                LOGGER.info("---------------ProductMasterDaoImpl Ends storeThumbnails----------------\n");

            } catch (SQLException e) {
                LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                throw new RuntimeException(e);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                        fis.close();
                    } catch (SQLException e) {
                        LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public List<DistributionList> getDistributionLists() {
        LOGGER.info("---------------ProductMasterDaoImpl Starts getDistributionLists----------------\n");
        String sql = "SELECT * FROM DistributionList ";

        Connection conn = null;
        List<DistributionList> listDistributionList=new ArrayList<DistributionList>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                DistributionList distributionList=new DistributionList(rs.getString("ListId"),rs.getString("ListName"),rs.getString("Owner"),rs.getString("CreatedTime"),rs.getString("LastModifiedTime"));
                listDistributionList.add(distributionList);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProductMasterDaoImpl Ends getDistributionLists {}", listDistributionList.size());

            return listDistributionList;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<UpcFacingDetail> getUpcForList(String listId) {
        LOGGER.info("---------------ProductMasterDaoImpl Starts getUpcForList----------------\n");
        String sql = "select IA.UPC , PRODUCT_SHORT_NAME, PRODUCT_LONG_NAME, BRAND_NAME from ProductMaster join (SELECT upc FROM DistributionListMapping where listId = ? ) IA on ProductMaster.UPC = IA.upc";

        Connection conn = null;
        List<UpcFacingDetail> listUpc=new ArrayList<UpcFacingDetail>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,listId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                listUpc.add(new UpcFacingDetail(rs.getString("upc"),"1",rs.getString("PRODUCT_SHORT_NAME"),rs.getString("PRODUCT_LONG_NAME"),rs.getString("BRAND_NAME")));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProductMasterDaoImpl Ends listUpc {}", listUpc.size());

            return listUpc;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }

	@Override
	public void createUpc(ProductMaster upcInput) {
		LOGGER.info("---------------ProductMasterDaoImpl Starts createUpc::categoryInput= {}", upcInput);
        String sql = "INSERT INTO ProductMaster (UPC,MFG_NAME,BRAND_NAME,PRODUCT_TYPE,PRODUCT_SHORT_NAME,PRODUCT_LONG_NAME,Attribute_1,Attribute_2,Attribute_3,Attribute_4,brandLevel,why_buy_1,why_buy_2,why_buy_3,why_buy_4,romance_copy_1,romance_copy_2,romance_copy_3,romance_copy_4,height,width,depth,product_rating,PRODUCT_SUB_TYPE) "
        		+ "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Connection conn = null;
        Integer id = -1;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, upcInput.getUpc());
            ps.setString(2, upcInput.getMfg_name());
            ps.setString(3, upcInput.getBrand_name());
            ps.setString(4, upcInput.getProduct_type());
            ps.setString(5, upcInput.getProduct_short_name());
            ps.setString(6, upcInput.getProduct_long_name());
            ps.setString(7, upcInput.getAttribute_1());
            ps.setString(8, upcInput.getAttribute_2());
            ps.setString(9, upcInput.getAttribute_3());
            ps.setString(10, upcInput.getAttribute_4());
            ps.setString(11, upcInput.getBrandLevel());
            ps.setString(12, upcInput.getWhy_buy_1());
            ps.setString(13, upcInput.getWhy_buy_2());
            ps.setString(14, upcInput.getWhy_buy_3());
            ps.setString(15, upcInput.getWhy_buy_4());
            ps.setString(16, upcInput.getRomance_copy_1());
            ps.setString(17, upcInput.getRomance_copy_2());
            ps.setString(18, upcInput.getRomance_copy_3());
            ps.setString(19, upcInput.getRomance_copy_4());
            ps.setString(20, upcInput.getHeight());
            ps.setString(21, upcInput.getWidth());
            ps.setString(22, upcInput.getDepth());
            ps.setString(23, upcInput.getProduct_rating());
            ps.setString(24, upcInput.getProduct_sub_type());
            
            id = ps.executeUpdate();

            if (id == 0) {
                throw new SQLException("Creating UPC failed, no rows affected.");
            }

            ps.close();
            LOGGER.info("---------------ProductMasterDaoImpl Ends createUpc----------------\n");


        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

                }
            }
        }
		
	}
}
