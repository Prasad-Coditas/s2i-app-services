package com.snap2buy.themobilebackend.dao.impl;

import com.snap2buy.themobilebackend.dao.MetaServiceDao;
import com.snap2buy.themobilebackend.dao.ProcessImageDao;
import com.snap2buy.themobilebackend.mapper.BeanMapper;
import com.snap2buy.themobilebackend.model.*;
import com.snap2buy.themobilebackend.util.Constants;
import com.snap2buy.themobilebackend.util.ConverterUtil;
import com.snap2buy.themobilebackend.util.ExpressionEvaluator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Map.Entry;

import static com.snap2buy.themobilebackend.util.ConverterUtil.ifNullToEmpty;

/**
 * Created by sachin on 10/17/15.
 */
@Component(value = BeanMapper.BEAN_IMAGE_STORE_DAO)
@Scope("prototype")
public class ProcessImageDaoImpl implements ProcessImageDao {

	private static final String MAX_UPC_CONFIDENCE = "1.0";
	
	private Logger LOGGER = LoggerFactory.getLogger(this.getClass());
    
	@Autowired
    private DataSource dataSource;
    
    @Autowired
    @Qualifier(BeanMapper.BEAN_META_SERVICE_DAO)
    private MetaServiceDao metaServiceDao;

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    private static final Map<String,List<String>> IMAGE_QUALITY_ISSUE_TYPE_MAP = new HashMap<String,List<String>>();
	static {
		IMAGE_QUALITY_ISSUE_TYPE_MAP.put("1", Arrays.asList(new String[] {"Photos taken from side","#9E6050"}));
		IMAGE_QUALITY_ISSUE_TYPE_MAP.put("2", Arrays.asList(new String[] {"Poor lighting","#C8B8A2"}));
		IMAGE_QUALITY_ISSUE_TYPE_MAP.put("3", Arrays.asList(new String[] {"Overexposed or blurry","#EED202"}));
		IMAGE_QUALITY_ISSUE_TYPE_MAP.put("4", Arrays.asList(new String[] {"Too far from fixture","#FF9966"}));
		IMAGE_QUALITY_ISSUE_TYPE_MAP.put("5", Arrays.asList(new String[] {"Person face in photo","#CC3300"}));
	}	

    @Override
    public void insert(ImageStore imageStore) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts insert {}", imageStore);
        long currTimestamp = System.currentTimeMillis() / 1000L;

        String sql = "insert into ImageStoreNew (imageUUID, userId, ImageFilePath, categoryId, latitude, longitude, timeStamp, storeId, hostId, dateId, imageStatus, shelfStatus, origWidth, origHeight, newWidth, newHeight, thumbnailPath, projectId, taskId, agentId, lastUpdatedTimestamp, fileId, questionId, imageURL, previewPath, resultUploaded, sequenceNumber)  VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageStore.getImageUUID());
            ps.setString(2, imageStore.getUserId());
            ps.setString(3, imageStore.getImageFilePath());
            ps.setString(4, imageStore.getCategoryId());
            ps.setString(5, imageStore.getLatitude());
            ps.setString(6, imageStore.getLongitude());
            ps.setString(7, imageStore.getTimeStamp());
            ps.setString(8, imageStore.getStoreId());
            ps.setString(9, imageStore.getHostId());
            ps.setString(10, imageStore.getDateId());
            ps.setString(11, imageStore.getImageStatus());
            ps.setString(12, imageStore.getShelfStatus());
            ps.setString(13, imageStore.getOrigWidth());
            ps.setString(14, imageStore.getOrigHeight());
            ps.setString(15, imageStore.getNewWidth());
            ps.setString(16, imageStore.getNewHeight());
            ps.setString(17, imageStore.getThumbnailPath());
            ps.setInt(18, imageStore.getProjectId());
            ps.setString(19, imageStore.getTaskId());
            ps.setString(20, imageStore.getAgentId());
            ps.setString(21, String.valueOf(currTimestamp));
            ps.setString(22, imageStore.getFileId());
            ps.setString(23, imageStore.getQuestionId());
            ps.setString(24, imageStore.getImageURL());
            ps.setString(25,imageStore.getPreviewPath());
            ps.setString(26,imageStore.getResultUploaded());
            ps.setString(27,imageStore.getSequenceNumber());
            
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends insert----------------\n");

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

    @Override
    public ImageStore findByImageUUId(String imageUUId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts findByImageUUId::imageUUId={}",imageUUId);
        String sql = "SELECT * FROM ImageStoreNew WHERE imageUUID = ?";

        Connection conn = null;
        ImageStore imageStore = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                imageStore = new ImageStore(rs.getString("ImageUUID"),
                        rs.getString("ImageFilePath"),
                        rs.getString("CategoryId"),
                        rs.getString("Latitude"),
                        rs.getString("Longitude"),
                        rs.getString("TimeStamp"),
                        rs.getString("StoreId"),
                        rs.getString("HostId"),
                        rs.getString("dateId"),
                        rs.getString("imageStatus"),
                        rs.getString("shelfStatus"),
                        rs.getString("origWidth"),
                        rs.getString("origHeight"),
                        rs.getString("newWidth"),
                        rs.getString("newHeight"),
                        rs.getString("thumbnailPath"),
                        rs.getString("userId"),
                        rs.getString("taskId"),
                        rs.getString("agentId"),
                        rs.getString("lastUpdatedTimestamp"),
                        rs.getString("imageHashScore"),
                        rs.getString("imageRotation"),
                        rs.getString("fileId"),
                        rs.getString("questionId"),
                        rs.getString("imageResultCode"),
                        rs.getString("imageReviewStatus"),
                        rs.getString("imageURL"),
                        rs.getString("processedDate"),
                        rs.getString("imageResultComments"),
                        rs.getString("resultUploaded"),
                        rs.getString("previewPath"),
                        rs.getInt("projectId")
                );
            }
            rs.close();
            ps.close();
            LOGGER.info("ProcessImageDaoImpl Starts findByImageUUId result = " + imageStore.getImageUUID());

            return imageStore;
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
    public ImageStore getImageByStatus(String shelfStatus) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageByStatus::shelfStatus= {}",shelfStatus);
        String sql = "SELECT * FROM ImageStoreNew WHERE shelfStatus = ?";

        Connection conn = null;
        ImageStore imageStore = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, shelfStatus);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {

                imageStore = new ImageStore(rs.getString("ImageUUID"),
                        rs.getString("ImageFilePath"),
                        rs.getString("CategoryId"),
                        rs.getString("Latitude"),
                        rs.getString("Longitude"),
                        rs.getString("TimeStamp"),
                        rs.getString("StoreId"),
                        rs.getString("HostId"),
                        rs.getString("dateId"),
                        rs.getString("imageStatus"),
                        rs.getString("shelfStatus"),
                        rs.getString("origWidth"),
                        rs.getString("origHeight"),
                        rs.getString("newWidth"),
                        rs.getString("newHeight"),
                        rs.getString("thumbnailPath"),
                        rs.getString("userId"),
                        rs.getString("taskId"),
                        rs.getString("agentId"),
                        rs.getString("lastUpdatedTimestamp"),
                        rs.getString("imageHashScore"),
                        rs.getString("imageRotation"),
                        rs.getString("fileId"),
                        rs.getString("questionId"),
                        rs.getString("imageResultCode"),
                        rs.getString("imageReviewStatus"),
                        rs.getString("imageURL"),
                        rs.getString("processedDate"),
                        rs.getString("imageResultComments"),
                        rs.getString("resultUploaded"),
                        rs.getString("previewPath"),
                        rs.getInt("projectId")
                );
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageByStatus result= {}",imageStore.getImageUUID());

            return imageStore;
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
    public Integer getJobCount(String shelfStatus) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getJobCount::shelfStatus={}",shelfStatus);
        String sql = "SELECT count(*) FROM ImageStoreNew WHERE shelfStatus = ?";

        Connection conn = null;
        ImageStore imageStore = null;
        try {
            int numberOfRows = 0;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, shelfStatus);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                numberOfRows = rs.getInt(1);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getJobCount numberOfRows = {} ",numberOfRows);

            return numberOfRows;
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
    public Integer getCronJobCount() {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getCronJobCount----------------\n");
        String sql = "SELECT count(*) FROM ImageStoreNew WHERE imageStatus in (\"cron\",\"cron1\",\"cron2\")";

        Connection conn = null;
        ImageStore imageStore = null;
        try {
            int numberOfRows = 0;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                numberOfRows = rs.getInt(1);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getCronJobCount numberOfRows ={} ",numberOfRows);

            return numberOfRows;
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
    public void updateStatusAndHost(String hostId, String shelfStatus, String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateStatusAndHost::hostId={} ::shelfStatus={}::imageUUID={}",hostId, shelfStatus, imageUUID);
        String sql = "UPDATE ImageStoreNew SET shelfStatus = ? , hostId = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, shelfStatus);
            ps.setString(2, hostId);
            ps.setString(3, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateStatusAndHost----------------\n");

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

    @Override
    public void updateShelfAnalysisStatus(String shelfStatus, String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateShelfAnalysisStatus::shelfStatus={}::imageUUID={}", shelfStatus, imageUUID);
        String sql = "UPDATE ImageStoreNew SET shelfStatus = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, shelfStatus);
            ps.setString(2, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateShelfAnalysisStatus----------------\n");


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

    @Override
    public void updateImageAnalysisStatus(String imageStatus, String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageAnalysisStatus::imageStatus={}::imageUUID={}", imageStatus, imageUUID);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageStatus = ?, lastUpdatedTimestamp = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageStatus);
            ps.setString(2, String.valueOf(currTimestamp));
            ps.setString(3, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageAnalysisStatus----------------\n");


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

    @Override
    public void updateImageAnalysisHostId(String hostId, String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageAnalysisStatus::hostId={}::imageUUID={}",hostId,imageUUID);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET hostId = ?, lastUpdatedTimestamp = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, hostId);
            ps.setString(2, String.valueOf(currTimestamp));
            ps.setString(3, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageAnalysisStatus----------------\n");


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

    @Override
    public void updateOrientationDetails(ImageStore imageStore) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateOrientationDetails::imageRotation={}::imageHashScore={}::orig-height::{} ::orig-width::{}::new-height::{} ::new-width::{}",imageStore.getImageRotation(),imageStore.getImageHashScore(),imageStore.getOrigHeight(),imageStore.getOrigWidth(),imageStore.getNewHeight(),imageStore.getNewWidth());
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageRotation = ?, imageHashScore = ?, origWidth = ?, origHeight = ?, newWidth = ?, newHeight = ?, lastUpdatedTimestamp = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageStore.getImageRotation());
            ps.setString(2, imageStore.getImageHashScore());
            ps.setString(3, imageStore.getOrigWidth());
            ps.setString(4, imageStore.getOrigHeight());
            ps.setString(5, imageStore.getNewWidth());
            ps.setString(6, imageStore.getNewHeight());
            ps.setString(7, String.valueOf(currTimestamp));
            ps.setString(8, imageStore.getImageUUID());

            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateOrientationDetails----------------\n");

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
    @Override
    public void updateStoreId(String storeId, String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateStatus::storeId={}::imageUUID={}",storeId,imageUUID);
        String sql = "UPDATE ImageStoreNew SET storeId = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeId);
            ps.setString(2, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateStatus----------------\n");


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

    @Override
     public List<ImageAnalysis> getImageAnalysis(String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageAnalysis::imageUUID={}",imageUUID);
        String sql = "select ImageAnalysisNew.id, ImageAnalysisNew.imageUUID, ImageAnalysisNew.taskId, ImageAnalysisNew.shelfLevel, ImageAnalysisNew.storeId, ImageAnalysisNew.projectId,"
        		+ "ImageAnalysisNew.dateId, ImageAnalysisNew.upc, ImageAnalysisNew.upcConfidence, ImageAnalysisNew.alternateUpc, ImageAnalysisNew.alternateUpcConfidence, "
        		+ "ImageAnalysisNew.leftTopX, ImageAnalysisNew.leftTopY, ImageAnalysisNew.width, ImageAnalysisNew.height, ImageAnalysisNew.promotion, ImageAnalysisNew.price, "
        		+ "ImageAnalysisNew.priceLabel, ImageAnalysisNew.priceConfidence, ImageAnalysisNew.compliant, ProductMaster.PRODUCT_SHORT_NAME, ProductMaster.PRODUCT_LONG_NAME, ProductMaster.BRAND_NAME "
        		+ "from ImageAnalysisNew LEFT JOIN ProductMaster ON ImageAnalysisNew.upc = ProductMaster.UPC where ImageAnalysisNew.imageUUID = ? ";
        List<ImageAnalysis> ImageAnalysisList=new ArrayList<ImageAnalysis>();
        Connection conn = null;
        ImageAnalysis imageAnalysis = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                imageAnalysis = new ImageAnalysis(rs.getString("imageUUID"), rs.getString("storeId"),
                        rs.getString("dateId"), rs.getString("upc"),
                        rs.getString("upcConfidence"), rs.getString("alternateUpc"),
                        rs.getString("alternateUpcConfidence"), rs.getString("leftTopX"),
                        rs.getString("leftTopY"), rs.getString("width"), rs.getString("height"),
                        rs.getString("promotion"), rs.getString("price"), rs.getString("priceLabel"),
                        rs.getString("PRODUCT_SHORT_NAME"), rs.getString("PRODUCT_LONG_NAME"),
                        rs.getString("BRAND_NAME"), rs.getString("priceConfidence"),
                        rs.getString("taskId"), rs.getInt("projectId"), rs.getString("id"));
                imageAnalysis.setShelfLevel(rs.getString("shelfLevel"));
                imageAnalysis.setCompliant(rs.getString("compliant"));
                ImageAnalysisList.add(imageAnalysis);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageAnalysis numberOfRows = {}",ImageAnalysisList.size());

            return ImageAnalysisList;
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
     public List<ImageAnalysis> getImageAnalysisForRecompute(String imageUUID) {
    	LOGGER.info("---------------ProcessImageDaoImpl Starts getImageAnalysisForRecompute::imageUUID={}",imageUUID);
        String sql = "select ImageAnalysisNew.id, ImageAnalysisNew.imageUUID, ImageAnalysisNew.taskId, ImageAnalysisNew.projectId, "
        		+ "ImageAnalysisNew.storeId, ImageAnalysisNew.dateId, ImageAnalysisNew.upc, "
        		+ "ImageAnalysisNew.upcConfidence, ImageAnalysisNew.alternateUpc, ImageAnalysisNew.alternateUpcConfidence, "
        		+ "ImageAnalysisNew.leftTopX, ImageAnalysisNew.leftTopY, ImageAnalysisNew.width, ImageAnalysisNew.height, "
        		+ "ImageAnalysisNew.promotion, ImageAnalysisNew.price, ImageAnalysisNew.priceLabel, ImageAnalysisNew.priceConfidence "
        		+ "from ImageAnalysisNew where ImageAnalysisNew.imageUUID = ? ";
        
        List<ImageAnalysis> ImageAnalysisList=new ArrayList<ImageAnalysis>();
        Connection conn = null;
        ImageAnalysis imageAnalysis = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                imageAnalysis = new ImageAnalysis(rs.getString("imageUUID"), rs.getString("storeId"),
                        rs.getString("dateId"), rs.getString("upc"),
                        rs.getString("upcConfidence"), rs.getString("alternateUpc"),
                        rs.getString("alternateUpcConfidence"), rs.getString("leftTopX"),
                        rs.getString("leftTopY"), rs.getString("width"), rs.getString("height"),
                        rs.getString("promotion"), rs.getString("price"),
                        rs.getString("priceLabel"),"PRODUCT_SHORT_NAME", "PRODUCT_LONG_NAME", "BRAND_NAME",
                        rs.getString("priceConfidence"), rs.getString("taskId"),
                        rs.getInt("projectId"), rs.getString("id"));
                ImageAnalysisList.add(imageAnalysis);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageAnalysisForRecompute numberOfRows = {}",ImageAnalysisList.size());

            return ImageAnalysisList;
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
    public String getImageAnalysisStatus(String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageAnalysisStatus::imageUUID={}",imageUUID);
        String sql = "SELECT imageStatus FROM ImageStoreNew WHERE imageUUID = ?";
        Connection conn = null;
        String imageStatus="queryFailed";
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                imageStatus = rs.getString("imageStatus");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageAnalysisStatus imageStatus = {}",imageStatus);

            return imageStatus;
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
    public ImageStore getNextImageDetails() {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getNextImageDetails----------------\n");
        String sql = "SELECT * FROM ImageStoreNew WHERE imageStatus in (\"cron\",\"cron1\",\"cron2\") order by lastUpdatedTimestamp limit 1";
        Connection conn = null;
        ImageStore imageStore = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                imageStore = new ImageStore(
                        rs.getString("ImageUUID"),
                        rs.getString("ImageFilePath"),
                        rs.getString("CategoryId"),
                        rs.getString("Latitude"),
                        rs.getString("Longitude"),
                        rs.getString("TimeStamp"),
                        rs.getString("StoreId"),
                        rs.getString("HostId"),
                        rs.getString("dateId"),
                        rs.getString("imageStatus"),
                        rs.getString("shelfStatus"),
                        rs.getString("origWidth"),
                        rs.getString("origHeight"),
                        rs.getString("newWidth"),
                        rs.getString("newHeight"),
                        rs.getString("thumbnailPath"),
                        rs.getString("userId"),
                        rs.getString("taskId"),
                        rs.getString("agentId"),
                        rs.getString("lastUpdatedTimestamp"),
                        rs.getString("imageHashScore"),
                        rs.getString("imageRotation"),
                        rs.getString("fileId"),
                        rs.getString("questionId"),
                        rs.getString("imageResultCode"),
                        rs.getString("imageReviewStatus"),
                        rs.getString("imageURL"),
                        rs.getString("processedDate"),
                        rs.getString("imageResultComments"),
                        rs.getString("resultUploaded"),
                        rs.getString("previewPath"),
                        rs.getInt("projectId")
                );
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl End getNextImageDetails----------------\n");

            return imageStore;
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
    public ImageStore getNextImageAndUpdateStatusAndHost(String hostId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getNextImageAndUpdateStatusAndHost----------------\n");
        String sql = "SELECT * FROM ImageStoreNew WHERE imageStatus in (\"cron\",\"cron1\",\"cron2\") order by categoryId, lastUpdatedTimestamp limit 1 for update";
        Connection conn = null;
        ImageStore imageStore = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement ps = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                imageStore = new ImageStore(
                        rs.getString("ImageUUID"),
                        rs.getString("ImageFilePath"),
                        rs.getString("CategoryId"),
                        rs.getString("Latitude"),
                        rs.getString("Longitude"),
                        rs.getString("TimeStamp"),
                        rs.getString("StoreId"),
                        rs.getString("HostId"),
                        rs.getString("dateId"),
                        rs.getString("imageStatus"),
                        rs.getString("shelfStatus"),
                        rs.getString("origWidth"),
                        rs.getString("origHeight"),
                        rs.getString("newWidth"),
                        rs.getString("newHeight"),
                        rs.getString("thumbnailPath"),
                        rs.getString("userId"),
                        rs.getString("taskId"),
                        rs.getString("agentId"),
                        rs.getString("lastUpdatedTimestamp"),
                        rs.getString("imageHashScore"),
                        rs.getString("imageRotation"),
                        rs.getString("fileId"),
                        rs.getString("questionId"),
                        rs.getString("imageResultCode"),
                        rs.getString("imageReviewStatus"),
                        rs.getString("imageURL"),
                        rs.getString("processedDate"),
                        rs.getString("imageResultComments"),
                        rs.getString("resultUploaded"),
                        rs.getString("previewPath"),
                        rs.getInt("projectId")
  );

                String imageStatus = "error"; //if analysis failed after retries, set the status to error to stop retry.
                if (imageStore.getImageStatus().equalsIgnoreCase("cron")) { //logic to stop infinite retries, only 2 retries after first attempt failed.
                    imageStatus = "processing";
                } else if (imageStore.getImageStatus().equalsIgnoreCase("cron1")) {
                    imageStatus = "processing1";
                } else if (imageStore.getImageStatus().equalsIgnoreCase("cron2")) {
                    imageStatus = "processing2";
                }
                rs.updateString("imageStatus", imageStatus);
                rs.updateString("hostId",hostId);
                rs.updateRow();
            }
            rs.close();
            ps.close();
            
            conn.commit();
            
            LOGGER.info("---------------ProcessImageDaoImpl End getNextImageAndUpdateStatusAndHost----------------\n");

            return imageStore;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    public List<String> getImagesForProcessing(String batchSize) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImagesForProcessing::batchSize={}----------------\n",batchSize);
        String sql = "SELECT imageUUID FROM ImageStoreNew WHERE imageStatus IN ('cron','cron1','cron2') ORDER BY categoryId, lastUpdatedTimestamp LIMIT ?";
        Connection conn = null;
        List<String> images = new ArrayList<String>();
        try {
            conn = dataSource.getConnection();
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(batchSize));
            
            ResultSet rs = ps.executeQuery();
            
            while(rs.next()) {
                images.add(rs.getString("imageUUID"));
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------ProcessImageDaoImpl End getImagesForProcessing----------------\n");

            return images;
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

    //change to insert multiple statement at one go
    @Override
	public void storeImageAnalysis(List<ImageAnalysis> imageAnalysisList, ImageStore imageStore) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts storeImageAnalysis::ImageAnalysisList size={} ::imageStore={}" ,imageAnalysisList.size(),imageStore);
        String sql = "INSERT IGNORE INTO ImageAnalysisNew (imageUUID, projectId, storeId, dateId, upc, upcConfidence, alternateUpc, alternateUpcConfidence, leftTopX, leftTopY, width, height, promotion, price, priceLabel, priceConfidence, taskId, shelfLevel, compliant, isDuplicate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Connection conn = null;
		
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(sql);
			for (ImageAnalysis imageAnalysis : imageAnalysisList) {
				ps.setString(1, imageStore.getImageUUID());
				ps.setInt(2, imageStore.getProjectId());
				ps.setString(3, imageStore.getStoreId());
				ps.setString(4, imageStore.getDateId());
				ps.setString(5, imageAnalysis.getUpc());
				ps.setString(6, imageAnalysis.getUpcConfidence());
				ps.setString(7, imageAnalysis.getAlternateUpc());
				ps.setString(8, imageAnalysis.getAlternateUpcConfidence());
				ps.setString(9, imageAnalysis.getLeftTopX());
				ps.setString(10, imageAnalysis.getLeftTopY());
				ps.setString(11, imageAnalysis.getWidth());
				ps.setString(12, imageAnalysis.getHeight());
				ps.setString(13, imageAnalysis.getPromotion());
				ps.setString(14, imageAnalysis.getPrice());
				ps.setString(15, imageAnalysis.getPriceLabel());
				ps.setString(16, imageAnalysis.getPriceConfidence());
				ps.setString(17, imageStore.getTaskId());
				ps.setString(18, imageAnalysis.getShelfLevel());
				ps.setString(19, imageAnalysis.getCompliant());
				ps.setString(20, imageAnalysis.getIsDuplicate());
				ps.addBatch();
			}
			ps.executeBatch();
			ps.close();

			conn.commit();
			
		} catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
				}
			}
		}

		LOGGER.info("---------------ProcessImageDaoImpl Ends storeImageAnalysis----------------\n");
	}
        
    @Override
    public List<LinkedHashMap<String,String>> getImages(String storeId, String dateId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImages::storeId={}::dateId={}", storeId,dateId);
        String sql = "SELECT imageUUID FROM ImageStoreNew WHERE storeId = ? and dateId = ?";
        List<LinkedHashMap<String,String>> imageStoreList=new ArrayList<LinkedHashMap<String,String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, storeId);
            ps.setString(2, dateId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("imageUUID", rs.getString("imageUUID"));
                imageStoreList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImages numberOfRows = {}",imageStoreList);

            return imageStoreList;
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
    public LinkedHashMap<String,Object> getFacing(String imageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getFacing::imageUUID={}",imageUUID);
        String sql = "select IA.UPC , PRODUCT_SHORT_NAME, PRODUCT_LONG_NAME, BRAND_NAME, IA.count from ProductMaster join (SELECT upc, count(*) as count FROM ImageAnalysisNew WHERE imageUUID = ? group by upc) IA on ProductMaster.UPC = IA.upc;";
        LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("upc"),new UpcFacingDetail(rs.getString("upc"),rs.getString("count"),rs.getString("PRODUCT_SHORT_NAME"),rs.getString("PRODUCT_LONG_NAME"),rs.getString("BRAND_NAME")));
                }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getFacing numberOfRows: {}" , map.keySet().size());

            return map;
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
    public List<LinkedHashMap<String,String>> doShareOfShelfAnalysis(String getImageUUIDCsvString) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getFacing::getImageUUIDCsvString={}",getImageUUIDCsvString);

        String baseSql = "select upc, max(facing) as facing, PRODUCT_SHORT_NAME, PRODUCT_LONG_NAME, BRAND_NAME from  (select ImageAnalysisNew.imageUUID as imageUUID, ImageAnalysisNew.upc, count(*) as facing, ProductMaster.PRODUCT_SHORT_NAME, ProductMaster.PRODUCT_LONG_NAME, ProductMaster.BRAND_NAME from ImageAnalysisNew, ProductMaster where ImageAnalysisNew.upc = ProductMaster.UPC and ImageAnalysisNew.imageUUID IN (";

        StringBuilder builder = new StringBuilder();
        builder.append(baseSql);

        for( String entry: getImageUUIDCsvString.split(",")) {
            builder.append("?,");
        }

        String sql = builder.deleteCharAt(builder.length() -1).toString()+") group by ImageAnalysisNew.upc, ImageAnalysisNew.imageUUID order by ProductMaster.BRAND_NAME) a group by upc";
        LOGGER.info("---------------ProcessImageDaoImpl Starts getFacing::sql={}",sql);

       // LinkedHashMap<String,Object> map=new LinkedHashMap<String,Object>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            int i=1;

            for( String entry: getImageUUIDCsvString.split(",")) {
                ps.setString(i++, entry);
            }

            ResultSet rs = ps.executeQuery();
            String curr = "initialTestString";
            List<LinkedHashMap<String,String>> multipleImageAnalysisList=new ArrayList<LinkedHashMap<String,String>>();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("upc", rs.getString("upc"));
                map.put("facing", rs.getString("facing"));
                map.put("productShortName", rs.getString("PRODUCT_SHORT_NAME"));
                map.put("productLongName", rs.getString("PRODUCT_LONG_NAME"));
                map.put("brandName", rs.getString("BRAND_NAME"));
                multipleImageAnalysisList.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getFacing numberOfRows = {}",multipleImageAnalysisList.size());
            return multipleImageAnalysisList;
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
    public void updateLatLong(String imageUUID, String latitude, String longitude) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateLatLong::imageUUID={}::latitude={}::longitude={}",imageUUID,latitude,longitude);
        String sql = "UPDATE ImageStoreNew SET latitude = ? , longitude = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, latitude);
            ps.setString(2, longitude);
            ps.setString(3, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateLatLong----------------\n");

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

    @Override
    public List<LinkedHashMap<String, String>> generateAggs(int projectId, String storeId, String taskId) throws SQLException {
		LOGGER.info("---------------ProcessImageDaoImpl Starts generateAggs::::projectId={}::storeId={}::taskId={}", projectId, storeId, taskId);

		String sqlFileName = "storeVisitAggregationType0.sql";
		String aggregationQuery = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");

		String deleteExistingAggDataQuery = "delete from ProjectStoreData where projectId = ? and storeId = ? and taskId =?";
		
		String deleteExistingShelfLevelDataQuery = "delete from ProjectStoreShelfLevelData where projectId = ? and storeId = ? and taskId =?";
		
		String insertAggDataQuery = "insert into ProjectStoreData "
				+ "(imageUUID,projectId,parentProjectId,storeId,taskId,retailerChainCode,retailer,city,stateCode,countryCode,facing,"
				+ "upc,manufacturer,brandName,productType,productSubType,skuTypeId,upcConfidence,priceConfidence,promotion,price,shelfLevel,"
				+ "visitDate,visitDay,visitWeekOfYear,visitMonth,visitQuarterOfYear,visitYear,productName,compliant) "
				+ "values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		String insertShelfLevelDataQuery = "insert into ProjectStoreShelfLevelData "
				+ "(projectId,storeId,taskId,upc,shelfLevel,facing) "
				+ "values "
				+ "(?,?,?,?,?,?)";
		
		Connection conn = null;
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement aggregationPs = conn.prepareStatement(aggregationQuery);
			PreparedStatement deleteExistingAggDataPs = conn.prepareStatement(deleteExistingAggDataQuery);
			PreparedStatement deleteExistingShelfLevelDataPs = conn.prepareStatement(deleteExistingShelfLevelDataQuery);
			PreparedStatement insertAggDataPs = conn.prepareStatement(insertAggDataQuery);
			PreparedStatement insertShelfLevelDataPs = conn.prepareStatement(insertShelfLevelDataQuery);

			aggregationPs.setInt(1, projectId);
			aggregationPs.setInt(2, projectId);
			aggregationPs.setString(3, storeId);
			aggregationPs.setString(4, taskId);
			aggregationPs.setInt(5, projectId);
			aggregationPs.setString(6, storeId);
			aggregationPs.setString(7, taskId);

			ResultSet rs = aggregationPs.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				map.put("imageUUID", rs.getString("imageUUID"));
				map.put("projectId", rs.getString("projectId"));
				map.put("parentProjectId", rs.getString("parentProjectId"));
				map.put("storeId", rs.getString("storeId"));
				map.put("taskId", rs.getString("taskId"));
				map.put("retailerChainCode", rs.getString("retailerChainCode"));
				map.put("retailer", rs.getString("retailer"));
				map.put("city", rs.getString("city"));
				map.put("stateCode", rs.getString("stateCode"));
				map.put("countryCode", rs.getString("countryCode"));
				map.put("facing", rs.getString("facing"));
				map.put("upc", rs.getString("upc"));
				map.put("manufacturer", rs.getString("manufacturer"));
				map.put("brandName", rs.getString("brandName"));
				map.put("productType", rs.getString("productType"));
				map.put("productSubType", rs.getString("productSubType"));
				map.put("skuTypeId", rs.getString("skuTypeId"));
				map.put("upcConfidence", rs.getString("upcConfidence"));
				map.put("priceConfidence", rs.getString("priceConfidence"));
				map.put("price", rs.getString("price"));
				map.put("promotion", rs.getString("promotion"));
				map.put("shelfLevel", "NA");
				map.put("visitDate", rs.getString("visitDate"));
				map.put("visitDay", rs.getString("visitDay"));
				map.put("visitWeekOfYear", rs.getString("visitWeekOfYear"));
				map.put("visitMonth", rs.getString("visitMonth"));
				map.put("visitQuarterOfYear", rs.getString("visitQuarterOfYear"));
				map.put("visitYear", rs.getString("visitYear"));
				map.put("productName", rs.getString("productName"));
				map.put("compliant", rs.getString("compliant"));
				map.put("TopShelfLevelCount", StringUtils.isNotBlank(rs.getString("topLevelCount")) ? rs.getString("topLevelCount") : "0" );
				map.put("MiddleShelfLevelCount", StringUtils.isNotBlank(rs.getString("middleLevelCount")) ? rs.getString("middleLevelCount") : "0" );
				map.put("BottomShelfLevelCount", StringUtils.isNotBlank(rs.getString("bottomLevelCount")) ? rs.getString("bottomLevelCount") : "0" );
				map.put("NAShelfLevelCount", StringUtils.isNotBlank(rs.getString("naLevelCount")) ? rs.getString("naLevelCount") : "0" );
				
				result.add(map);
					
				insertAggDataPs.setString(1, map.get("imageUUID"));
				insertAggDataPs.setInt(2, Integer.valueOf(map.get("projectId")));
				insertAggDataPs.setInt(3, Integer.valueOf(map.get("parentProjectId")));
				insertAggDataPs.setString(4, map.get("storeId"));
				insertAggDataPs.setString(5, map.get("taskId"));
				insertAggDataPs.setString(6, map.get("retailerChainCode"));
				insertAggDataPs.setString(7, map.get("retailer"));
				insertAggDataPs.setString(8, map.get("city"));
				insertAggDataPs.setString(9, map.get("stateCode"));
				insertAggDataPs.setString(10, map.get("countryCode"));
				insertAggDataPs.setString(11, map.get("facing"));
				insertAggDataPs.setString(12, map.get("upc"));
				insertAggDataPs.setString(13, map.get("manufacturer"));
				insertAggDataPs.setString(14, map.get("brandName"));
				insertAggDataPs.setString(15, map.get("productType"));
				insertAggDataPs.setString(16, map.get("productSubType"));
				insertAggDataPs.setString(17, map.get("skuTypeId"));
				insertAggDataPs.setString(18, map.get("upcConfidence"));
				insertAggDataPs.setString(19, map.get("priceConfidence"));
				insertAggDataPs.setString(20, map.get("promotion"));
				insertAggDataPs.setString(21, map.get("price"));
				insertAggDataPs.setString(22, map.get("shelfLevel"));
				insertAggDataPs.setString(23, map.get("visitDate"));
				insertAggDataPs.setString(24, map.get("visitDay"));
				insertAggDataPs.setString(25, map.get("visitWeekOfYear"));
				insertAggDataPs.setString(26, map.get("visitMonth"));
				insertAggDataPs.setString(27, map.get("visitQuarterOfYear"));
				insertAggDataPs.setString(28, map.get("visitYear"));
				insertAggDataPs.setString(29, map.get("productName"));
				insertAggDataPs.setString(30, map.get("compliant"));

				List<String> shelfLevels = new ArrayList<String>();
				for(String key : map.keySet()) {
					if ( key.endsWith("ShelfLevelCount") && !map.get(key).equals("0") ) {
						String shelfLevel = key.replace("ShelfLevelCount", "");
						shelfLevels.add(shelfLevel);
						insertShelfLevelDataPs.setInt(1, Integer.valueOf(map.get("projectId")));
						insertShelfLevelDataPs.setString(2, map.get("storeId"));
						insertShelfLevelDataPs.setString(3, map.get("taskId"));
						insertShelfLevelDataPs.setString(4, map.get("upc"));
						insertShelfLevelDataPs.setString(5, shelfLevel);
						insertShelfLevelDataPs.setInt(6, Integer.parseInt(map.get(key)));
						
						insertShelfLevelDataPs.addBatch();
					}
				}
				
				if ( !shelfLevels.isEmpty() ) {
					
					map.put("shelfLevel", shelfLevels.toString().replaceAll("\\[", "").replaceAll("\\]",""));
					insertAggDataPs.setString(22, map.get("shelfLevel"));
				}
				insertAggDataPs.addBatch();
			}
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType0::DELETEAGG::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			deleteExistingAggDataPs.setInt(1, projectId);
			deleteExistingAggDataPs.setString(2, storeId);
			deleteExistingAggDataPs.setString(3, taskId);
			deleteExistingAggDataPs.execute();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType0::DELETESHELFLEVEL::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			deleteExistingShelfLevelDataPs.setInt(1, projectId);
			deleteExistingShelfLevelDataPs.setString(2, storeId);
			deleteExistingShelfLevelDataPs.setString(3, taskId);
			deleteExistingShelfLevelDataPs.execute();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType0::INSERTAGG::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			insertAggDataPs.executeBatch();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType0::INSERTSHELFLEVEL::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			insertShelfLevelDataPs.executeBatch();
			
			conn.commit();

			rs.close();
			aggregationPs.close();
			deleteExistingAggDataPs.close();
			deleteExistingShelfLevelDataPs.close();
			insertAggDataPs.close();
			insertShelfLevelDataPs.close();
			
			LOGGER.info("---------------ProcessImageDaoImpl Ends generateAggs----------------\n");
			return result;
		} catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
				}
			}
		}
	}
    
    @Override
    public List<LinkedHashMap<String, String>> generateAggsType1(int projectId, String storeId, String taskId) throws SQLException {
		LOGGER.info("---------------ProcessImageDaoImpl Starts generateAggsType1::::projectId={}::storeId={}::taskId={}", projectId, storeId, taskId);
		
		String sqlFileName = "storeVisitAggregationType1.sql";
		String aggregationQuery = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");

		String deleteExistingAggDataQuery = "delete from ProjectStoreData where projectId = ? and storeId = ? and taskId =?";
		
		String deleteExistingShelfLevelDataQuery = "delete from ProjectStoreShelfLevelData where projectId = ? and storeId = ? and taskId =?";
		
		String insertAggDataQuery = "insert into ProjectStoreData "
				+ "(imageUUID,projectId,parentProjectId,storeId,taskId,retailerChainCode,retailer,city,stateCode,countryCode,facing,"
				+ "upc,manufacturer,brandName,productType,productSubType,skuTypeId,upcConfidence,priceConfidence,promotion,price,shelfLevel,"
				+ "visitDate,visitDay,visitWeekOfYear,visitMonth,visitQuarterOfYear,visitYear,productName,compliant) "
				+ "values "
				+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		String insertShelfLevelDataQuery = "insert into ProjectStoreShelfLevelData "
				+ "(projectId,storeId,taskId,upc,shelfLevel,facing) "
				+ "values "
				+ "(?,?,?,?,?,?)";
		
		Connection conn = null;
		List<LinkedHashMap<String, String>> result = new ArrayList<LinkedHashMap<String, String>>();
		try {
			conn = dataSource.getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement aggregationPs = conn.prepareStatement(aggregationQuery);
			PreparedStatement deleteExistingAggDataPs = conn.prepareStatement(deleteExistingAggDataQuery);
			PreparedStatement deleteExistingShelfLevelDataPs = conn.prepareStatement(deleteExistingShelfLevelDataQuery);
			PreparedStatement insertAggDataPs = conn.prepareStatement(insertAggDataQuery);
			PreparedStatement insertShelfLevelDataPs = conn.prepareStatement(insertShelfLevelDataQuery);

			aggregationPs.setInt(1, projectId);
			aggregationPs.setInt(2, projectId);
			aggregationPs.setString(3, storeId);
			aggregationPs.setString(4, taskId);
			aggregationPs.setInt(5, projectId);
			aggregationPs.setString(6, storeId);
			aggregationPs.setString(7, taskId);
			aggregationPs.setInt(8, projectId);
			aggregationPs.setString(9, storeId);
			aggregationPs.setString(10, taskId);

			ResultSet rs = aggregationPs.executeQuery();
			while (rs.next()) {
				LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
				map.put("imageUUID", rs.getString("imageUUID"));
				map.put("projectId", rs.getString("projectId"));
				map.put("parentProjectId", rs.getString("parentProjectId"));
				map.put("storeId", rs.getString("storeId"));
				map.put("taskId", rs.getString("taskId"));
				map.put("retailerChainCode", rs.getString("retailerChainCode"));
				map.put("retailer", rs.getString("retailer"));
				map.put("city", rs.getString("city"));
				map.put("stateCode", rs.getString("stateCode"));
				map.put("countryCode", rs.getString("countryCode"));
				map.put("facing", rs.getString("facing"));
				map.put("upc", rs.getString("upc"));
				map.put("manufacturer", rs.getString("manufacturer"));
				map.put("brandName", rs.getString("brandName"));
				map.put("productType", rs.getString("productType"));
				map.put("productSubType", rs.getString("productSubType"));
				map.put("skuTypeId", rs.getString("skuTypeId"));
				map.put("upcConfidence", rs.getString("upcConfidence"));
				map.put("priceConfidence", rs.getString("priceConfidence"));
				map.put("price", rs.getString("price"));
				map.put("promotion", rs.getString("promotion"));
				map.put("shelfLevel", "NA");
				map.put("visitDate", rs.getString("visitDate"));
				map.put("visitDay", rs.getString("visitDay"));
				map.put("visitWeekOfYear", rs.getString("visitWeekOfYear"));
				map.put("visitMonth", rs.getString("visitMonth"));
				map.put("visitQuarterOfYear", rs.getString("visitQuarterOfYear"));
				map.put("visitYear", rs.getString("visitYear"));
				map.put("productName", rs.getString("productName"));
				map.put("compliant", rs.getString("compliant"));
				map.put("TopShelfLevelCount", StringUtils.isNotBlank(rs.getString("topLevelCount")) ? rs.getString("topLevelCount") : "0" );
				map.put("MiddleShelfLevelCount", StringUtils.isNotBlank(rs.getString("middleLevelCount")) ? rs.getString("middleLevelCount") : "0" );
				map.put("BottomShelfLevelCount", StringUtils.isNotBlank(rs.getString("bottomLevelCount")) ? rs.getString("bottomLevelCount") : "0" );
				map.put("NAShelfLevelCount", StringUtils.isNotBlank(rs.getString("naLevelCount")) ? rs.getString("naLevelCount") : "0" );
				
				result.add(map);
					
				insertAggDataPs.setString(1, map.get("imageUUID"));
				insertAggDataPs.setInt(2, Integer.valueOf(map.get("projectId")));
				insertAggDataPs.setInt(3, Integer.valueOf(map.get("parentProjectId")));
				insertAggDataPs.setString(4, map.get("storeId"));
				insertAggDataPs.setString(5, map.get("taskId"));
				insertAggDataPs.setString(6, map.get("retailerChainCode"));
				insertAggDataPs.setString(7, map.get("retailer"));
				insertAggDataPs.setString(8, map.get("city"));
				insertAggDataPs.setString(9, map.get("stateCode"));
				insertAggDataPs.setString(10, map.get("countryCode"));
				insertAggDataPs.setString(11, map.get("facing"));
				insertAggDataPs.setString(12, map.get("upc"));
				insertAggDataPs.setString(13, map.get("manufacturer"));
				insertAggDataPs.setString(14, map.get("brandName"));
				insertAggDataPs.setString(15, map.get("productType"));
				insertAggDataPs.setString(16, map.get("productSubType"));
				insertAggDataPs.setString(17, map.get("skuTypeId"));
				insertAggDataPs.setString(18, map.get("upcConfidence"));
				insertAggDataPs.setString(19, map.get("priceConfidence"));
				insertAggDataPs.setString(20, map.get("promotion"));
				insertAggDataPs.setString(21, map.get("price"));
				insertAggDataPs.setString(22, map.get("shelfLevel"));
				insertAggDataPs.setString(23, map.get("visitDate"));
				insertAggDataPs.setString(24, map.get("visitDay"));
				insertAggDataPs.setString(25, map.get("visitWeekOfYear"));
				insertAggDataPs.setString(26, map.get("visitMonth"));
				insertAggDataPs.setString(27, map.get("visitQuarterOfYear"));
				insertAggDataPs.setString(28, map.get("visitYear"));
				insertAggDataPs.setString(29, map.get("productName"));
				insertAggDataPs.setString(30, map.get("compliant"));

				List<String> shelfLevels = new ArrayList<String>();
				for(String key : map.keySet()) {
					if ( key.endsWith("ShelfLevelCount") && !map.get(key).equals("0") ) {
						String shelfLevel = key.replace("ShelfLevelCount", "");
						shelfLevels.add(shelfLevel);
						insertShelfLevelDataPs.setInt(1, Integer.valueOf(map.get("projectId")));
						insertShelfLevelDataPs.setString(2, map.get("storeId"));
						insertShelfLevelDataPs.setString(3, map.get("taskId"));
						insertShelfLevelDataPs.setString(4, map.get("upc"));
						insertShelfLevelDataPs.setString(5, shelfLevel);
						insertShelfLevelDataPs.setInt(6, Integer.parseInt(map.get(key)));
						
						insertShelfLevelDataPs.addBatch();
					}
				}
				
				if ( !shelfLevels.isEmpty() ) {
					String step1 = StringUtils.join(shelfLevels, "\", \"");// Join with ", "
					String shelfLevelsString = StringUtils.wrap(step1, "\"");// Wrap step1 with "
					map.put("shelfLevel", shelfLevelsString);
					insertAggDataPs.setString(22, map.get("shelfLevel"));
				}
				insertAggDataPs.addBatch();
			}
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType1::DELETEAGG::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			deleteExistingAggDataPs.setInt(1, projectId);
			deleteExistingAggDataPs.setString(2, storeId);
			deleteExistingAggDataPs.setString(3, taskId);
			deleteExistingAggDataPs.execute();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType1::DELETESHELFLEVEL::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			deleteExistingShelfLevelDataPs.setInt(1, projectId);
			deleteExistingShelfLevelDataPs.setString(2, storeId);
			deleteExistingShelfLevelDataPs.setString(3, taskId);
			deleteExistingShelfLevelDataPs.execute();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType1::INSERTAGG::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			insertAggDataPs.executeBatch();
			
			LOGGER.info("ProcessImageDaoImpl::generateAggsType1::INSERTSHELFLEVEL::projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
			insertShelfLevelDataPs.executeBatch();
			
			conn.commit();

			rs.close();
			aggregationPs.close();
			deleteExistingAggDataPs.close();
			deleteExistingShelfLevelDataPs.close();
			insertAggDataPs.close();
			insertShelfLevelDataPs.close();
			
			LOGGER.info("---------------ProcessImageDaoImpl Ends generateAggsType1----------------\n");
			return result;
		} catch (SQLException e) {
            LOGGER.error("EXCEPTION {}", e.getMessage(), e);
			throw e;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
				}
			}
		}
		
	}

    @Override
    public Map<String, List<Map<String, String>>> getProjectStoreData(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreData::projectId={}::storeId={}", projectId, storeId);

        String distinctTaskIdsSql = null;
		if(null == taskId || taskId.equalsIgnoreCase("-9")){
            distinctTaskIdsSql = "select distinct(taskId) as taskId from ProjectStoreData where projectId= ? and storeId = ?";
        }

		String sql = "SELECT " + 
				"        A.imageUUID,A.projectId,A.storeId, A.taskId, A.upc, A.skuTypeId, A.facing, B.shelfLevel, A.upcConfidence,A.price,A.priceConfidence,A.promotion, A.BRAND_NAME,A.PRODUCT_SHORT_NAME,A.PRODUCT_LONG_NAME, A.PRODUCT_TYPE, A.PRODUCT_SUB_TYPE " + 
				" FROM " + 
				" (" + 
				"    SELECT " + 
				"        a.imageUUID,b.projectId,a.storeId, a.taskId, b.upc, b.skuTypeId, a.facing,a.upcConfidence,a.price,a.priceConfidence,a.promotion, b.BRAND_NAME,b.PRODUCT_SHORT_NAME,b.PRODUCT_LONG_NAME, b.PRODUCT_TYPE, b.PRODUCT_SUB_TYPE " + 
				"    FROM " + 
				"	    (SELECT * FROM  ProjectStoreData WHERE projectId = ? AND storeId = ? AND taskId = ?) a  " + 
				"    RIGHT OUTER JOIN  " + 
				"	    (SELECT ProjectUpc.upc, ProjectUpc.skuTypeId, ProjectUpc.projectId,ProductMaster.BRAND_NAME,ProductMaster.PRODUCT_SHORT_NAME,ProductMaster.PRODUCT_LONG_NAME, ProductMaster.PRODUCT_TYPE, ProductMaster.PRODUCT_SUB_TYPE FROM ProjectUpc LEFT JOIN ProductMaster ON ProjectUpc.UPC = ProductMaster.UPC" + 
				"		    WHERE projectId = ? AND skuTypeId NOT IN ('99') " + 
				"        ) b  " + 
				"    ON " + 
				"        (a.upc = b.upc)    " + 
				"        AND (a.projectId = b.projectId)" + 
				" ) A" + 
				" LEFT JOIN" + 
				" (" + 
				"    SELECT " + 
				"        projectId,storeId,taskId,upc,GROUP_CONCAT(shelfLevel order by (CASE shelfLevel WHEN 'Top' THEN 1 WHEN 'Middle' THEN 2  WHEN 'Bottom' THEN 3 WHEN 'NA' THEN 4 END) ASC SEPARATOR ', ' ) AS shelfLevel" + 
				"    FROM " + 
				"        ProjectStoreShelfLevelData" + 
				"    WHERE " + 
				"        projectId = ? AND storeId = ? AND taskId = ?" + 
				"    GROUP BY" + 
				"        projectId,storeId,taskId,upc" + 
				" ) B" + 
				" ON" + 
				" A.projectId = B.projectId AND A.storeId = B.storeId AND A.taskId = B.taskId AND A.upc = B.upc";

		Connection conn = null;
		List<String> taskIdList = new ArrayList<String>();

		Map<String, List<Map<String, String>>> result = new LinkedHashMap<String, List<Map<String, String>>>();

		try {
			conn = dataSource.getConnection();

			if(null != distinctTaskIdsSql){
                PreparedStatement distinctTaskPs = conn.prepareStatement(distinctTaskIdsSql);
                distinctTaskPs.setInt(1, projectId );
                distinctTaskPs.setString(2, storeId);
                ResultSet distinctTaskRs = distinctTaskPs.executeQuery();
                while (distinctTaskRs.next()) {
                    taskIdList.add(distinctTaskRs.getString("taskId"));
                }
                distinctTaskRs.close();
                distinctTaskPs.close();
            }else {
			    taskIdList.add(taskId.trim());
            }

			for (String singleTaskId : taskIdList) {
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();

				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, projectId);
				ps.setString(2, storeId);
				ps.setString(3, singleTaskId);
				ps.setInt(4, projectId);
				ps.setInt(5, projectId);
				ps.setString(6, storeId);
				ps.setString(7, singleTaskId);

				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					String skuTypeId = rs.getString("skuTypeId");
					String facing = rs.getString("facing");
					if ("0".equals(skuTypeId) && (StringUtils.isBlank(facing) || "0".equals(facing))) {
						continue;
					}
					map.put("imageUUID", rs.getString("imageUUID"));
					map.put("projectId", rs.getInt("projectId") + "");
					map.put("storeId", storeId);
					map.put("taskId", singleTaskId);
					map.put("upc", rs.getString("upc"));
					map.put("brand_name", rs.getString("BRAND_NAME"));
					map.put("product_short_name", rs.getString("PRODUCT_SHORT_NAME"));
					map.put("product_long_name", rs.getString("PRODUCT_LONG_NAME"));
					map.put("facing", facing);
					map.put("upcConfidence", rs.getString("upcConfidence"));
					map.put("price", rs.getString("price"));
					map.put("priceConfidence", rs.getString("priceConfidence"));
					map.put("promotion", rs.getString("promotion"));
					map.put("shelfLevel", ConverterUtil.ifNullToEmpty(rs.getString("shelfLevel")));
					map.put("product_type", rs.getString("PRODUCT_TYPE"));
                    map.put("product_sub_type", rs.getString("PRODUCT_SUB_TYPE"));
                    map.put("skuTypeId", skuTypeId);

					list.add(map);
				}
				rs.close();
				ps.close();

				result.put(singleTaskId, list);
			}

			LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreData----------------\n");
			return result;
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
    public List<LinkedHashMap<String, String>> getProjectTopStores(int projectId, String limit) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreResults::projectId= {} limit = {}", projectId, limit);
        String sql = "select storeId, projectId, count(distinct(upc)) as order1, sum(facing) as order2, sum(upcConfidence) as order3 from ProjectStoreData where projectId = ? and upc !=\"999999999999\" group by projectId, storeId order by order1 desc, order2 desc, order3 desc limit ?";
        Connection conn = null;
        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, Integer.valueOf(limit));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("storeId"));
                map.put("projectId", rs.getInt("projectId")+"");
                map.put("countDistinctUpc", rs.getString("order1"));
                map.put("sumFacing", rs.getString("order2"));
                map.put("sumUpcConfidence", rs.getString("order3"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectTopStores----------------\n");
            return result;
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
    public List<LinkedHashMap<String, String>> getProjectBottomStores(int projectId, String limit) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBottomStores::projectId={} limit ={}", projectId,limit);
        String sql = "select storeId, projectId, count(distinct(upc)) as order1, sum(facing) as order2, sum(upcConfidence) as order3 from ProjectStoreData where projectId = ? and upc !=\"999999999999\" group by projectId, storeId order by order1 asc, order2 asc, order3 asc limit ?";
        Connection conn = null;
        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.valueOf(projectId));
            ps.setInt(2, Integer.valueOf(limit));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeId", rs.getString("storeId"));
                map.put("projectId", rs.getInt("projectId")+"");
                map.put("countDistinctUpc", rs.getString("order1"));
                map.put("sumFacing", rs.getString("order2"));
                map.put("sumUpcConfidence", rs.getString("order3"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBottomStores----------------\n");
            return result;
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
	public List<LinkedHashMap<String, String>> getProjectStoreImages(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreImages::projectId={}::storeId={}::taskId={}", projectId,storeId,taskId);
        String sql = "SELECT imageUUID,dateId,agentId,taskId,imageStatus,imageReviewStatus,imageReviewRecommendations,imageNotUsable,imageNotUsableComment,lowConfidence"
        		+ " FROM ImageStoreNew WHERE projectId = ? and storeId = ? ";
        
        if( !taskId.equalsIgnoreCase("-9") ) {
        	sql = sql+" and taskId = ? ";
        }
        
        sql = sql + "order by lowConfidence desc";
        
        List<LinkedHashMap<String,String>> imageStoreList=new ArrayList<LinkedHashMap<String,String>>();
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            if( !taskId.equalsIgnoreCase("-9") ) {
            	ps.setString(3,taskId);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("imageUUID", rs.getString("imageUUID"));
                String dateId = rs.getString("dateId");
                if ( !dateId.isEmpty() ) {
                	try {
                			dateId = outSdf.format(inSdf.parse(dateId));
					} catch (ParseException e) {
						LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
					}
                }
                map.put("dateId", dateId);
                map.put("agentId", rs.getString("agentId"));
                map.put("taskId", rs.getString("taskId"));
                map.put("imageStatus", rs.getString("imageStatus"));
                map.put("imageReviewStatus",rs.getString("imageReviewStatus"));
                map.put("imageReviewRecommendations",ifNullToEmpty(rs.getString("imageReviewRecommendations")));
                map.put("imageNotUsable",rs.getString("imageNotUsable"));
                map.put("imageNotUsableComment",ifNullToEmpty(rs.getString("imageNotUsableComment")));
                map.put("lowConfidence",StringUtils.isNotBlank(rs.getString("lowConfidence")) ? rs.getString("lowConfidence") : "0" );

                imageStoreList.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImages numberOfRows = {}",imageStoreList);

            return imageStoreList;
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
	public List<StoreWithImages> getProjectStoresWithNoUPCs(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoresWithNoUPCs::projectId={}",projectId);
		
        String storesWithNoUPCSql = "select StoreMaster.storeId,StoreMaster.retailerStoreId,StoreMaster.retailerChainCode,StoreMaster.retailer,StoreMaster.street,StoreMaster.city,StoreMaster.stateCode,StoreMaster.state,StoreMaster.zip from StoreMaster JOIN "
        		+ "(select distinct(ImageStoreNew.storeId) from ImageStoreNew where ImageStoreNew.projectId= ? and ImageStoreNew.imageStatus = ? and ImageStoreNew.storeId not in "
        		+ "(select distinct(ProjectStoreData.storeId) from ProjectStoreData where ProjectStoreData.projectId= ?)) StoresWithNoUPC"
        		+ " ON StoreMaster.storeId = StoresWithNoUPC.storeId";
        
        String storeImagesWithNoUPCSql = "select imageUUID,dateId,agentId,taskId from ImageStoreNew where"
        		+ " storeId = ? and projectId = ? and imageStatus= ?;";
        
        List<StoreWithImages> storesWithNoUPCList=new ArrayList<StoreWithImages>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(storesWithNoUPCSql);
            ps.setInt(1, projectId);
            ps.setString(2, "done");
            ps.setInt(3, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	StoreWithImages store = new StoreWithImages();
            	store.setStoreId(rs.getString("storeId"));
            	store.setRetailerStoreId(rs.getString("retailerStoreId"));
            	store.setRetailerChainCode( rs.getString("retailerChainCode"));
            	store.setRetailer( rs.getString("retailer"));
            	store.setStreet(rs.getString("street"));
            	store.setCity( rs.getString("city"));
            	store.setStateCode( rs.getString("stateCode"));
            	store.setState(rs.getString("state"));
            	store.setZip( rs.getString("zip"));
                //find image count and imageUUIDs for this store
                PreparedStatement imageUUIDPs = conn.prepareStatement(storeImagesWithNoUPCSql);
                imageUUIDPs.setString(1, store.getStoreId());
                imageUUIDPs.setInt(2, projectId);
                imageUUIDPs.setString(3, "done");
                ResultSet imageUUIDRs = imageUUIDPs.executeQuery();
                int countOfImagesForStore = 0;
                List<StoreImageInfo> imageUUIDs = new ArrayList<StoreImageInfo>();
            	while (imageUUIDRs.next()){
            		countOfImagesForStore++;
            		StoreImageInfo imageInfo = new StoreImageInfo();
            		imageInfo.setImageUUID(imageUUIDRs.getString("imageUUID"));
            		imageInfo.setDateId(imageUUIDRs.getString("dateId"));
            		imageInfo.setAgentId(imageUUIDRs.getString("agentId"));
            		imageInfo.setTaskId(imageUUIDRs.getString("taskId"));
            		imageUUIDs.add(imageInfo);
                }
                imageUUIDRs.close();
                imageUUIDPs.close();
                store.setImageCount(String.valueOf(countOfImagesForStore));
                store.setImageUUIDs(imageUUIDs);
                storesWithNoUPCList.add(store);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoresWithNoUPCs numberOofStores = {}",storesWithNoUPCList.size());

            return storesWithNoUPCList;
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
	public List<StoreWithImages> getProjectAllStoreImages(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreImages::projectId={}",projectId);
		
        String storesForCustomerCodeProjectIdSql = "select StoreMaster.storeId,StoreMaster.retailerStoreId,StoreMaster.retailerChainCode,StoreMaster.retailer,StoreMaster.street,StoreMaster.city,StoreMaster.stateCode,StoreMaster.state,StoreMaster.zip from StoreMaster JOIN "
        		+ "(select distinct(ImageStoreNew.storeId) from ImageStoreNew where ImageStoreNew.projectId= ?) StoresForCustomerCodeProjectId"
        		+ " ON StoreMaster.storeId = StoresForCustomerCodeProjectId.storeId";
        
        String storeImagesSql = "select imageUUID,dateId,agentId,taskId from ImageStoreNew where"
        		+ " storeId = ? and projectId = ?";
        
        List<StoreWithImages> storesWithImages =new ArrayList<StoreWithImages>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(storesForCustomerCodeProjectIdSql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	StoreWithImages store = new StoreWithImages();
            	store.setStoreId(rs.getString("storeId"));
            	store.setRetailerStoreId(rs.getString("retailerStoreId"));
            	store.setRetailerChainCode( rs.getString("retailerChainCode"));
            	store.setRetailer( rs.getString("retailer"));
            	store.setStreet(rs.getString("street"));
            	store.setCity( rs.getString("city"));
            	store.setStateCode( rs.getString("stateCode"));
            	store.setState(rs.getString("state"));
            	store.setZip( rs.getString("zip"));
                //find image count and imageUUIDs for this store
                PreparedStatement imageUUIDPs = conn.prepareStatement(storeImagesSql);
                imageUUIDPs.setString(1, store.getStoreId());
                imageUUIDPs.setInt(2, projectId);
                ResultSet imageUUIDRs = imageUUIDPs.executeQuery();
                int countOfImagesForStore = 0;
                List<StoreImageInfo> imageUUIDs = new ArrayList<StoreImageInfo>();
            	while (imageUUIDRs.next()){
            		countOfImagesForStore++;
            		StoreImageInfo imageInfo = new StoreImageInfo();
            		imageInfo.setImageUUID(imageUUIDRs.getString("imageUUID"));
            		imageInfo.setDateId(imageUUIDRs.getString("dateId"));
            		imageInfo.setAgentId(imageUUIDRs.getString("agentId"));
            		imageInfo.setTaskId(imageUUIDRs.getString("taskId"));
            		imageUUIDs.add(imageInfo);
                }
                imageUUIDRs.close();
                imageUUIDPs.close();
                store.setImageCount(String.valueOf(countOfImagesForStore));
                store.setImageUUIDs(imageUUIDs);
                storesWithImages.add(store);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreImages numberOofStores = {}",storesWithImages.size());

            return storesWithImages;
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
	public List<DuplicateImages> getProjectStoresWithDuplicateImages(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoresWithDuplicateImages::projectId={}",projectId);
		
        String duplicateImagesSql = "SELECT imageStore.imageUUID, imageStore.storeId, imageStore.agentId, imageStore.taskId, imageStore.dateId, imageStore.imageHashScore, imageStore.imageURL, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip FROM ImageStoreNew imageStore"
        		+ " left join StoreMaster store on imageStore.storeId = store.storeId"
        		+ " WHERE imageHashScore IN (SELECT * FROM (SELECT imageHashScore FROM ImageStoreNew  where projectId= ? and imageHashScore is not null and imageHashScore > '0' GROUP BY imageHashScore HAVING COUNT(imageHashScore) > 1) AS a) and imageStore.projectId = ? order by imageHashScore";
        
        List<DuplicateImages> duplicateImagesList =new ArrayList<DuplicateImages>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(duplicateImagesSql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);
            ResultSet rs = ps.executeQuery();
            String previousHashScore = "dummyhashscore";
            DuplicateImages dupImages = null;
            int count = 1 ;
            while (rs.next()) {
            	String hashScore = rs.getString("imageHashScore");
            	if ( !hashScore.equalsIgnoreCase(previousHashScore) ) {
            		if ( null !=  dupImages ) {
            			//Logic to exclude duplicate images from same store
            			Map<String,DuplicateImageInfo> uniqueDupMap = new LinkedHashMap<String,DuplicateImageInfo>();
            			for ( DuplicateImageInfo duplicate : dupImages.getStoreIds() ) {
            				uniqueDupMap.put(duplicate.getStoreId(),duplicate);
            			}
            			if ( uniqueDupMap.size() > 1 ) {
            				dupImages.getStoreIds().clear();
            				dupImages.getStoreIds().addAll(uniqueDupMap.values());
            				duplicateImagesList.add(dupImages);	
            			} else {
            				count--; //Discard this group of images as it is from the same store, and reset count to previous value
            			}
            		}
            		dupImages = new DuplicateImages();
            		dupImages.setImageHashScore(hashScore);
            		dupImages.setSlNo(""+count);
            		count++;
            		previousHashScore = hashScore;
            	}
            	DuplicateImageInfo dup = new DuplicateImageInfo();
            	dup.setStoreId(rs.getString("storeId"));
            	dup.setRetailerStoreId(rs.getString("retailerStoreId"));
            	dup.setRetailerChainCode( rs.getString("retailerChainCode"));
            	dup.setRetailer( rs.getString("retailer"));
            	dup.setStreet(rs.getString("street"));
            	dup.setCity( rs.getString("city"));
            	dup.setStateCode( rs.getString("stateCode"));
            	dup.setState( rs.getString("state"));
            	dup.setZip( rs.getString("zip"));
            	dup.setImageUUID(rs.getString("imageUUID"));
            	dup.setDateId(rs.getString("dateId"));
            	dup.setAgentId( rs.getString("agentId"));
            	dup.setTaskId( rs.getString("taskId"));
            	dup.setImageURL(ifNullToEmpty(rs.getString("imageURL")));
            	dupImages.getStoreIds().add(dup);
            }
            if (  null != dupImages ) {
            	//Logic to exclude duplicate images from same store
    			Map<String,DuplicateImageInfo> uniqueDupMap = new LinkedHashMap<String,DuplicateImageInfo>();
    			for ( DuplicateImageInfo duplicate : dupImages.getStoreIds() ) {
    				uniqueDupMap.put(duplicate.getStoreId(),duplicate);
    			}
    			if ( uniqueDupMap.size() > 1 ) {
    				dupImages.getStoreIds().clear();
    				dupImages.getStoreIds().addAll(uniqueDupMap.values());
    				duplicateImagesList.add(dupImages);	
    			} else {
    				//do nothing - Discard this group of images as it is from the same store
    			}
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoresWithDuplicateImages number of duplicates = {}",duplicateImagesList.size());

            return duplicateImagesList;
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
	public List<LinkedHashMap<String, String>> generateStoreVisitResults(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts generateStoreVisitResults::projectId={}::storeId={} :: taskId={}", projectId,storeId,taskId);
		
		Map<String,Object> analysisData = getStoreLevelDataForAnalysis(projectId,storeId,taskId);
		List<String> aggUPCs = (List<String>) analysisData.get("aggUPCs");
		Map<String,Object> projectStoreData = (Map<String, Object>) analysisData.get("projectStoreData");
		Map<String,String> repResponses = (Map<String, String>) analysisData.get("repResponses");
		String countDistinctUpc = (String) analysisData.get("countDistinctUpc");
		String percentageOsa = (String) analysisData.get("percentageOsa");
		String distributionPercentage = (String) analysisData.get("distributionPercentage");
		String hasLowQualityImages = (String) analysisData.get("hasLowQualityImages");
		String hasLowConfidenceDetections = (String) analysisData.get("hasLowConfidenceDetections");
		String sumFacing = (String) analysisData.get("sumFacing");
		String sumUpcConfidence = (String) analysisData.get("sumUpcConfidence");
		String countMissingUPC = (String) analysisData.get("countMissingUPC");
		String previewImageUUID = (String) analysisData.get("previewImageUUID");
		
		
		//calculate resultCode based on UPC findings
		LOGGER.info("---------------ProcessImageDaoImpl--generateStoreVisitResults:: calculating store results ------------------");
		List<ProjectStoreGradingCriteria> storeGradingCriteriaList = metaServiceDao.getProjectStoreGradingCriterias(projectId);
		Map<String,List<ProjectStoreGradingCriteria>> storeGradingCriteriaGroupedByResultCode = new LinkedHashMap<String,List<ProjectStoreGradingCriteria>>();
		for ( ProjectStoreGradingCriteria gradingCriteria : storeGradingCriteriaList ) {
			if ( storeGradingCriteriaGroupedByResultCode.get(""+gradingCriteria.getResultCode()) == null ) {
				storeGradingCriteriaGroupedByResultCode.put(""+gradingCriteria.getResultCode(), new ArrayList<ProjectStoreGradingCriteria>());
			}
			storeGradingCriteriaGroupedByResultCode.get(""+gradingCriteria.getResultCode()).add(gradingCriteria);
		}
		
		List<ProjectStoreGradingCriteria> satisfiedCriteriaList = 
				calculateStoreResult(aggUPCs, storeGradingCriteriaGroupedByResultCode, projectStoreData, repResponses, countDistinctUpc,
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
		
		String resultCode = null, status = null, resultComment = "";
		if ( satisfiedCriteriaList.isEmpty() ) {
			status = "0";
			resultCode = "98"; //FAILED DUE TO NO MATCH
			resultComment = "No criterias defined or none matched";
		} else {
			for (ProjectStoreGradingCriteria criteria : satisfiedCriteriaList) {
				resultCode = ""+criteria.getResultCode();
				status = ""+criteria.getStoreStatus();
				if ( StringUtils.isNotBlank(criteria.getCriteriaComment())) {
					resultComment = resultComment + criteria.getCriteriaComment() + ",";
				}
			}
		}
		if ( StringUtils.isNotBlank(resultComment)) {
			resultComment = resultComment.substring(0, resultComment.length() - 1);
		}
		
		LinkedHashMap<String,String> storeResultParametersToUpdate = new LinkedHashMap<String,String>();
		storeResultParametersToUpdate.put("countDistinctUpc", countDistinctUpc);
		storeResultParametersToUpdate.put("sumFacing", sumFacing);
		storeResultParametersToUpdate.put("sumUpcConfidence", sumUpcConfidence);
		storeResultParametersToUpdate.put("resultCode", resultCode);
		storeResultParametersToUpdate.put("status", status);
		storeResultParametersToUpdate.put("resultComment", resultComment);
		storeResultParametersToUpdate.put("countMissingUPC", countMissingUPC);
		storeResultParametersToUpdate.put("percentageOsa", percentageOsa);

		LOGGER.info("---------------ProcessImageDaoImpl--generateStoreVisitResults::Store Result Status :: {}" , storeResultParametersToUpdate);
		
        updateStoreResultParamtersForProjectStoreVisit(projectId, storeId,taskId, storeResultParametersToUpdate);

		//Find and update best preview image for this store visit. If not found (due to no agg data), use one of the images as preview image.
		String bestPreviewImageUUID = findPreviewImageUUIDForStoreVisit(projectId, storeId, taskId);
		if ( StringUtils.isNotBlank(bestPreviewImageUUID) ) {
			previewImageUUID = bestPreviewImageUUID;
		}
		
		if ( StringUtils.isNotBlank(previewImageUUID) ) {
			updatePreviewImageUUIDForStoreVisit(projectId, storeId, taskId, previewImageUUID);
		} else {
			LOGGER.error("---------------ProcessImageDaoImpl--generateStoreVisitResults::Unable to find preview image UUID for this store visit. Image UUID will not be updated in result table ------------------");
		}
		
		storeResultParametersToUpdate.put("projectId", ""+projectId);
		storeResultParametersToUpdate.put("storeId", storeId);
		storeResultParametersToUpdate.put("taskId", taskId);
		List<LinkedHashMap<String,String>> result = new ArrayList<LinkedHashMap<String,String>>();
		result.add(storeResultParametersToUpdate);
		LOGGER.info("---------------ProcessImageDaoImpl--Ends generateStoreVisitResults---------------");
		return result;
	}
	
	@Override
	public Map<String,Object> getStoreLevelDataForAnalysis(int projectId, String storeId, String taskId) {
		// get data from ProjectStoreData table for this customer code, project id and store combination
		List<String> aggUPCs = getDistinctUPCsForProject(projectId, storeId,taskId);
		LOGGER.info("---------------ProcessImageDaoImpl--getStoreLevelDataForAnalysis::UPCs in agg table :: {}" , aggUPCs );
				
		Map<String,Object> projectStoreData =getAggregatedData(projectId, storeId, taskId);
		String countDistinctUpc = ""+projectStoreData.get("count");
		String sumFacing = ""+projectStoreData.get("facing");
		String sumUpcConfidence = ""+projectStoreData.get("confidence");
		String oosFacings;
		String percentageOsa = "0";
		String distributionPercentage = "0";
		String hasLowQualityImages = "false";
		String hasLowConfidenceDetections = "false";
				
		Map<String,String> repResponses = getRepResponsesByStoreVisit(projectId, storeId, taskId);
				
		//Set value to 0 if null or empty.. Set precision to 10 digits in case of decimals
		countDistinctUpc = (null==countDistinctUpc || "".equals(countDistinctUpc)) ? "0" : countDistinctUpc;
		sumFacing = (null==sumFacing || "".equals(sumFacing)) ? "0" : sumFacing;
		sumUpcConfidence = (null==sumUpcConfidence || "".equals(sumUpcConfidence)) ? "0" : sumUpcConfidence;
		if ( sumUpcConfidence != "0" && sumUpcConfidence.contains(".") ) {
			String[] confidenceParts = sumUpcConfidence.split("\\.");
			if ( confidenceParts[1].length() > 9 ) {
				sumUpcConfidence = confidenceParts[0] + "." + confidenceParts[1].substring(0,10);
			}
		}
		oosFacings = sumOfOosFacings(projectId, storeId, taskId);
		//percentageOsa = String.valueOf(Float.parseFloat(countDistinctUpc) / (Float.parseFloat(countDistinctUpc) + Float.parseFloat(countMissingUPC)));
		percentageOsa = String.valueOf( (Float.parseFloat(sumFacing)-Float.parseFloat(oosFacings))/(Float.parseFloat(sumFacing)));
		distributionPercentage = getDistributionPercentageByStoreVisit(projectId, storeId, taskId);
		hasLowQualityImages = hasLowQualityImages(projectId, storeId, taskId);
		hasLowConfidenceDetections = hasLowConfidenceDetections(projectId, storeId, taskId);
		List<String> imageUUIDs = getImagesByStoreVisit(projectId, storeId, taskId);
		String previewImageUUID = "";
		if (imageUUIDs.size() > 0 ) {
			previewImageUUID = imageUUIDs.get(0);
		}
		
		Map<String,Object> dataMap = new HashMap<String,Object>();
		dataMap.put("aggUPCs", aggUPCs);
		dataMap.put("projectStoreData", projectStoreData);
		dataMap.put("repResponses", repResponses);
		dataMap.put("countDistinctUpc", countDistinctUpc);
		dataMap.put("percentageOsa", percentageOsa);
		dataMap.put("distributionPercentage", distributionPercentage);
		dataMap.put("hasLowQualityImages", hasLowQualityImages);
		dataMap.put("hasLowConfidenceDetections", hasLowConfidenceDetections);
		dataMap.put("sumFacing",sumFacing);
		dataMap.put("sumUpcConfidence",sumUpcConfidence);
		dataMap.put("countMissingUPC",oosFacings);
		dataMap.put("previewImageUUID", previewImageUUID);
		
		return dataMap;

	}

	private String hasLowQualityImages(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts hasLowQualityImages::projectId={} :: storeId={} :: taskId={}",projectId,storeId,taskId);
		String hasLowQualityImages = "false";
		String query = "SELECT COUNT(*) AS lowQualityPhotoCount FROM ImageStoreNew WHERE"
				+ " projectId=? AND storeId=? AND taskId=? AND imageNotUsable = '1' AND imageReviewStatus <> '1'";
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        if(rs.next()) {
	        		int lowQualityPhotCount = rs.getInt("lowQualityPhotoCount");
	        		if (lowQualityPhotCount > 0 ) {
	        			hasLowQualityImages = "true";
	        		}
	        }
	        rs.close();
	        ps.close();
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
	    LOGGER.info("---------------ProcessImageDaoImpl Ends hasLowQualityImages with value {}----------------\n", hasLowQualityImages);
	    return hasLowQualityImages;
	}
	
	private String hasLowConfidenceDetections(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts hasLowConfidenceDetections::projectId={} :: storeId={} :: taskId={}",projectId,storeId,taskId);
		String hasLowConfidenceDetections = "false";
		String query = "SELECT COUNT(*) AS lowConfidenceDetectionCount FROM ImageStoreNew WHERE"
				+ " projectId=? AND storeId=? AND taskId=? AND lowConfidence = '1' AND imageReviewStatus <> '1'";
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        if(rs.next()) {
	        		int lowConfidenceDetectionCount = rs.getInt("lowConfidenceDetectionCount");
	        		if (lowConfidenceDetectionCount > 0 ) {
	        			hasLowConfidenceDetections = "true";
	        		}
	        }
	        rs.close();
	        ps.close();
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
	    LOGGER.info("---------------ProcessImageDaoImpl Ends hasLowConfidenceDetections with value {}----------------\n", hasLowConfidenceDetections);
	    return hasLowConfidenceDetections;
	}

	private String getDistributionPercentageByStoreVisit(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getDistributionPercentageByStoreVisit::projectId={} :: storeId={} :: taskId={}",projectId,storeId,taskId);
		String distributionPercentage = "0.0";
		String query = "SELECT ROUND(distribution,3) AS distributionPercentage FROM ProjectStoreResult WHERE projectId=? AND storeId=? AND taskId=?";
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        if(rs.next()) {
	        		distributionPercentage = rs.getString("distributionPercentage");
	        		if (StringUtils.isBlank(distributionPercentage)) {
	        			distributionPercentage = "0.0";
	        		}
	        }
	        rs.close();
	        ps.close();
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
	    LOGGER.info("---------------ProcessImageDaoImpl Ends getDistributionPercentageByStoreVisit with value {}----------------\n", distributionPercentage);
	    return distributionPercentage;
	}

	private String findPreviewImageUUIDForStoreVisit(int projectId, String storeId,
			String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts findPreviewImageUUIDForStoreVisit::projectId={} :: storeId={} :: taskId={}",projectId,storeId,taskId);
		//pick the imageUUID which is appearing against most number of UPCs in project store data for this store visit
		//if there are two or more, then use one with highest sum of facings to break the tie
		String previewImageUUID = null;
		String query = "SELECT imageUUID, count(distinct(upc)) as upcCount, sum(facing) as facingCount FROM ProjectStoreData"
				+ " WHERE projectId = ? AND storeId = ? AND taskId = ? AND upc != \"999999999999\" AND imageUUID IS NOT NULL"
				+ " GROUP BY projectId, storeId, taskId, imageUUID ORDER BY upcCount DESC, facingCount DESC LIMIT 1";
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(query);
	        ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        if(rs.next()) {
	        		previewImageUUID = rs.getString("imageUUID");
	        }
	        rs.close();
	        ps.close();
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
	    LOGGER.info("---------------ProcessImageDaoImpl::findPreviewImageUUIDForStoreVisit:: Preview Image UUID={}",previewImageUUID);
	    LOGGER.info("---------------ProcessImageDaoImpl Ends findPreviewImageUUIDForStoreVisit----------------\n");
	    return previewImageUUID;
	}

	@Override
	public List<LinkedHashMap<String, String>> insertOrUpdateStoreResult(
            int projectId, String storeId,
			String countDistinctUpc, String sumFacing, String sumUpcConfidence,
			String resultCode, String status, String agentId, String taskId,
			String visitDateId, String imageUrl, String batchId, String customerProjectId) {
		
		String updateStoreResultSql = "UPDATE ProjectStoreResult SET resultCode = ?, countDistinctUpc = ?, sumFacing = ?, sumUpcConfidence = ?, status = ? WHERE projectId = ? and storeId = ? and taskId= ?";
		
		String insertStoreResultSql = "INSERT INTO ProjectStoreResult (projectId, storeId, resultCode, countDistinctUpc,sumFacing,sumUpcConfidence, status, agentId, taskId, visitDateId, batchId, customerProjectId) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
		
		String insertStoreResultWithUrlSql = "INSERT INTO ProjectStoreResult (projectId, storeId, resultCode, countDistinctUpc,sumFacing,sumUpcConfidence, status, agentId, taskId, visitDateId, batchId, customerProjectId, imageURL) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		boolean isStoreAvailableInStoreResults = isStoreAvailableInStoreResults(projectId, storeId, taskId);
		
		//update/insert resultCode based on UPC findings.
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        
	        boolean withoutURL = ( imageUrl != null && imageUrl.equals("dummyURL") );
	        
	        if ( isStoreAvailableInStoreResults ) { //if there exists an entry for the store already
	        	if ( withoutURL ) {//AND input URL is dummyURL i.e. call from store result generation
		        	PreparedStatement updatePs = conn.prepareStatement(updateStoreResultSql);
		        	updatePs.setString(1, resultCode);
		        	updatePs.setInt(2, Integer.parseInt(countDistinctUpc));
		        	updatePs.setInt(3, Integer.parseInt(sumFacing));
		        	updatePs.setBigDecimal(4, new BigDecimal(sumUpcConfidence));
		        	updatePs.setString(5, status);
		        	updatePs.setInt(6, projectId);
                    updatePs.setString(7, storeId);
                    updatePs.setString(8, taskId);
		        	updatePs.executeUpdate();
		        	updatePs.close();
	        	}
	        } else {
	        	PreparedStatement insertPs = null;
	        	if ( withoutURL ) {
	        		insertPs = conn.prepareStatement(insertStoreResultSql);	
	        	} else {
	        		insertPs = conn.prepareStatement(insertStoreResultWithUrlSql);
	        	}
	        	
	        	insertPs.setInt(1, projectId);
	        	insertPs.setString(2, storeId);
	        	insertPs.setString(3, resultCode);
	        	insertPs.setInt(4, Integer.parseInt(countDistinctUpc));
	        	insertPs.setInt(5, Integer.parseInt(sumFacing));
	        	insertPs.setBigDecimal(6, new BigDecimal(sumUpcConfidence));
	        	insertPs.setString(7, status);
	        	insertPs.setString(8, agentId);
                insertPs.setString(9, taskId);
                insertPs.setString(10, visitDateId);
                insertPs.setString(11, batchId);
                insertPs.setString(12, customerProjectId);
	        	if ( !withoutURL ) {
		        	insertPs.setString(13, imageUrl);
	        	}
	        	insertPs.executeUpdate();
	        	insertPs.close();
	        }
	        
	        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
	        LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
	        map.put("storeId", storeId);
	        map.put("resultCode", resultCode);
            map.put("status", status);
            map.put("taskId", taskId);
	        result.add(map);
	        LOGGER.info("---------------ProcessImageDaoImpl Ends generateStoreVisitResults result = {}",map);
	        return result;
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
	public void updatePreviewImageUUIDForStoreVisit(int projectId, String storeId,
			String taskId, String previewImageUUID) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updatePreviewImageUUIDForStoreVisit----------------\n");
		String updatePreviewImageSql = "UPDATE ProjectStoreResult SET ImageUUID = ? WHERE projectId = ? and storeId = ? and taskId= ?";
		Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updatePreviewImageSql);
            ps.setString(1, previewImageUUID);
            ps.setInt(2, projectId);
            ps.setString(3, storeId);
            ps.setString(4, taskId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updatePreviewImageUUIDForStoreVisit----------------\n");

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
	
	@Override
	public boolean isStoreAvailableInStoreResults(int projectId, String storeId, String taskId) {
		
		String doResultExistsSql = "SELECT COUNT(*) AS COUNT FROM ProjectStoreResult WHERE projectId = ? and storeId = ? and taskId=?";
		int count = 0;
		
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement doResultExistsPs = conn.prepareStatement(doResultExistsSql);
	        doResultExistsPs.setInt(1, projectId);
            doResultExistsPs.setString(2, storeId);
            doResultExistsPs.setString(3, taskId);
	        ResultSet doResultExistsRs = doResultExistsPs.executeQuery();
	        
	        if (doResultExistsRs.next()) {
	        	count = Integer.parseInt(doResultExistsRs.getString("COUNT"));
	        }
	        doResultExistsRs.close();
	        doResultExistsPs.close();
	        
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
	    
	    if ( count > 0 ) {
        	return true;
        } else {
        	return false;
        }
	}
	
	private Map<String, Object> getAggregatedData(int projectId, String storeId,String taskId) {
		Map<String,Object> projectStoreData = new LinkedHashMap<String,Object>();
		String countFacingConfidenceSql = " select count(distinct(upc)) as count, coalesce(sum(facing),0) as facing, coalesce(sum(upcConfidence),0) as confidence from ProjectStoreData "
				+ "where projectId=? and storeId=? and taskId=? and upc !=\"999999999999\" and skuTypeId in ('1','2','3')";

		String upcFacingSql = " select upc,brandName,facing,price,promotion,skuTypeId from ProjectStoreData where projectId=? and storeId=? and taskId=? and upc !=\"999999999999\"";
		
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(countFacingConfidenceSql);
	        ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        if (rs.next()) {
	        	projectStoreData.put("count", rs.getInt("count"));
	        	projectStoreData.put("facing",rs.getInt("facing") );
	        	projectStoreData.put("confidence", rs.getString("confidence") );
	        }
	        rs.close();
	        ps.close();
	        
	        PreparedStatement ps1 = conn.prepareStatement(upcFacingSql);
	        ps1.setInt(1, projectId);
            ps1.setString(2, storeId);
            ps1.setString(3, taskId);
	        ResultSet rs1 = ps1.executeQuery();
	        
	        Map<String,Map<String,String>> upcLevelData = new HashMap<String,Map<String,String>>();
	        Map<String,Integer> brandLevelData = new HashMap<String,Integer>();
	        
	        while(rs1.next()) {
	        	Map<String,String> oneUpcData = new HashMap<String,String>();
	        	oneUpcData.put("facing",rs1.getString("facing"));
	        	oneUpcData.put("price",rs1.getString("price"));
	        	oneUpcData.put("promotion",rs1.getString("promotion"));
	        	upcLevelData.put(rs1.getString("upc"), oneUpcData);
	        	
	        	String brandName = ifNullToEmpty(rs1.getString("brandName")).toUpperCase();
	        	String skuTypeId = ifNullToEmpty(rs1.getString("skuTypeId"));
	        	if ( StringUtils.isNotBlank(skuTypeId) && Arrays.asList("1","2","3").contains(skuTypeId) ) {
	        		Integer brandLevelFacing = brandLevelData.get(brandName);
		        	if ( brandLevelFacing == null ) {
		        		brandLevelData.put(brandName, Integer.parseInt(rs1.getString("facing")));
		        	} else {
		        		brandLevelData.put(brandName,brandLevelFacing + Integer.parseInt(rs1.getString("facing")));
		        	}
	        	}
	        }
	        rs1.close();
	        ps1.close();
	        
	        projectStoreData.put("UPCs",upcLevelData);
	        projectStoreData.put("brands", brandLevelData);
	        
	        LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreData ----------------\n");

	        return projectStoreData;
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

	private List<ProjectStoreGradingCriteria> calculateStoreResult(List<String> aggUPCs, Map<String,List<ProjectStoreGradingCriteria>> storeGradingCriteriaGroupedByResultCode,
			Map<String, Object> projectStoreData, Map<String, String> repResponses, String countDistinctUpc, String percentageOsa, 
			String distributionPercentage, String hasLowQualityImages, String hasLowConfidenceDetections) {
		LOGGER.info("---------------ProcessImageDaoImpl--calculateStoreResult--------------");
		List<ProjectStoreGradingCriteria> satisfiedCriteriaList = new ArrayList<ProjectStoreGradingCriteria>();
		
		for( Entry<String, List<ProjectStoreGradingCriteria>> gradingCriteriaListForResultCode :  storeGradingCriteriaGroupedByResultCode.entrySet()) {
			LOGGER.info("---------------ProcessImageDaoImpl--calculateStoreResult:: evaluating criterias for result code:: {}", gradingCriteriaListForResultCode.getKey());
			boolean atleastOneCriteriaMet = false;
			for (ProjectStoreGradingCriteria gradingCriteria : gradingCriteriaListForResultCode.getValue()) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateStoreResult:: evaluating criteria :: {}", gradingCriteria.getCriteriaExpression());
				if ( StringUtils.isNotBlank(gradingCriteria.getCriteriaExpression()) ) {
					boolean result = ExpressionEvaluator.evaluate(gradingCriteria.getCriteriaExpression(), aggUPCs, projectStoreData, repResponses, countDistinctUpc,
							percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
					if ( result ) {
						satisfiedCriteriaList.add(gradingCriteria);
						LOGGER.info("Found matching criteria = {}", gradingCriteria);
						atleastOneCriteriaMet = true;
					}
				}
			}
			if ( atleastOneCriteriaMet ) { break; }
		}
		return satisfiedCriteriaList;
	}
	
	private List<String> getDistinctUPCsForProject(int projectId, String storeId, String taskId) {
		List<String> upcList = new ArrayList<String>();
		String distinctUPCSql = "select distinct(UPC) as UPC from ProjectStoreData where projectId=? and storeId=? and taskId= ? and upc !=\"999999999999\"";
		
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(distinctUPCSql);
	        ps.setInt(1, projectId);
	        ps.setString(2, storeId);
            ps.setString(3, taskId);
	        ResultSet rs = ps.executeQuery();
	        
	        while (rs.next()) {
	        	upcList.add(rs.getString("UPC"));
	        }
	        rs.close();
	        ps.close();
	        LOGGER.info("---------------ProcessImageDaoImpl Ends getDistinctUPCsForProject ----------------\n");

	        return upcList;
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
	public List<LinkedHashMap<String, String>> getProjectAllStoreResults(int projectId, String level, String value) {
		 LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreResults::projectId={}, level={}, value={}",projectId,level,value);
	        String sql = "SELECT result.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip, store.Latitude, store.Longitude,"
	        		+ " result.resultCode, result.resultComment, result.countDistinctUpc, result.sumFacing, result.sumUpcConfidence, result.status, result.imageURL, result.agentId, result.taskId,"
	        		+ " DATE_FORMAT(result.processedDate,'%m/%d/%Y') as processedDate, result.visitDateId, result.imageUUID, result.linearFootage, result.countMissingUpc, result.percentageOsa,"
	        		+ " psgc.criteriaName, psgc.resultColor"
	        		+ " FROM ProjectStoreResult result "  
	        		+ " INNER JOIN ( SELECT a.projectId, a.storeId, MAX(concat(a.visitDateId,a.taskId)) as maxVisitTask FROM ProjectStoreResult a WHERE a.projectId = ? AND a.visitDateId LIKE ? AND COALESCE(a.waveId,'') LIKE ? GROUP BY a.projectId, a.storeId ) maxresult" 
	        		+ "	ON result.projectId = maxresult.projectId AND result.storeId = maxresult.storeId AND concat(result.visitDateId,result.taskId) <=> maxresult.maxVisitTask "
	        		+ "	LEFT JOIN StoreMaster store ON result.storeId = store.storeId" 
	        		+ " LEFT JOIN ProjectStoreGradingCriteria psgc ON result.projectId = psgc.projectId AND result.resultCode = psgc.resultCode" 
	        		+ "	WHERE result.projectId=? AND result.visitDateId LIKE ? AND COALESCE(result.waveId,'') LIKE ?" 
	        		+ " GROUP BY result.storeId, result.taskId,result.resultCode" 
	        		+ "	ORDER BY result.resultCode asc, result.countDistinctUpc desc, result.sumFacing desc, result.sumUpcConfidence desc";
	        
	        Connection conn = null;
	        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
	        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
	        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	        
	        String monthFilter = "%";
	        String waveFilter = "%";
	        
	        if ( StringUtils.isNotBlank(level) && level.equals("wave") ) {
		        waveFilter = value;
	        }
	        
	        if ( StringUtils.isNotBlank(level) && level.equals("month") ) {
	        	// month will be of format 01/18 i.e. MM/YY, need to convert it to YYYYMM
	    		// this logic won't work by 2100 :)
	    		String[] parts = value.split("/");
	    		value = "20" + parts[1] + parts[0];
	        	monthFilter = value+monthFilter; 
	        }
	        
	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement ps = conn.prepareStatement(sql);
	            ps.setInt(1, projectId);
	            ps.setString(2, monthFilter);
	            ps.setString(3, waveFilter);
	            ps.setInt(4, projectId);
	            ps.setString(5, monthFilter);
	            ps.setString(6, waveFilter);
	            
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	                map.put("storeId", ifNullToEmpty(rs.getString("storeId")));
	                map.put("retailerStoreId", ifNullToEmpty(rs.getString("retailerStoreId")));
	                map.put("retailerChainCode", ifNullToEmpty(rs.getString("retailerChainCode")));
	                map.put("retailer", ifNullToEmpty(rs.getString("retailer")));
	                map.put("street", ifNullToEmpty(rs.getString("street")));
	                map.put("city", ifNullToEmpty(rs.getString("city")));
	                map.put("stateCode", ifNullToEmpty(rs.getString("stateCode")));
	                map.put("state", ifNullToEmpty(rs.getString("state")));
	                map.put("zip", ifNullToEmpty(rs.getString("zip")));
	                map.put("lat", ifNullToEmpty(rs.getString("Latitude")));
	                map.put("long", ifNullToEmpty(rs.getString("Longitude")));
	                map.put("agentId", ifNullToEmpty(rs.getString("agentId")));
	                map.put("taskId", ifNullToEmpty(rs.getString("taskId")));
                    map.put("linearFootage", rs.getString("linearFootage"));
                    map.put("countMissingUpc", rs.getString("countMissingUpc"));
                    map.put("percentageOsa", rs.getString("percentageOsa"));

	                String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
	                if ( !visitDate.isEmpty() ) {
	                	try {
							visitDate = outSdf.format(inSdf.parse(visitDate));
						} catch (ParseException e) {
                            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
						}
	                }
	                map.put("visitDate", visitDate);
	                map.put("processedDate", ifNullToEmpty(rs.getString("processedDate")));
	                map.put("resultCode", ifNullToEmpty(rs.getString("resultCode")));
	                map.put("result", ifNullToEmpty(rs.getString("criteriaName")));
	                map.put("resultColor", ifNullToEmpty(rs.getString("resultColor")));
	                map.put("resultComment", ifNullToEmpty(rs.getString("resultComment")));
	                map.put("countDistinctUpc", ifNullToEmpty(String.valueOf(rs.getInt("countDistinctUpc"))));
	                map.put("sumFacing", ifNullToEmpty(String.valueOf(rs.getInt("sumFacing"))));
	                map.put("sumUpcConfidence", ifNullToEmpty(String.valueOf(rs.getBigDecimal("sumUpcConfidence"))));
	                map.put("status", ifNullToEmpty(rs.getString("status")));
                    map.put("imageUUID", ifNullToEmpty(rs.getString("imageUUID")));
	                String imageUrl = rs.getString("imageURL");
	                if (imageUrl == null || imageUrl.isEmpty() ) {
	                	imageUrl = "Not Available";
	                }
	                map.put("imageURL", imageUrl );
	                result.add(map);
	            }
	            rs.close();
	            ps.close();
	            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreResults----------------\n");
	            return result;
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
	public List<LinkedHashMap<String, String>> getProjectAllStoreResultsDetail(int projectId) {
		 LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreResultsDetail::customerCode={}", projectId);
	     List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
	     SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
	     outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	     SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	     inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

	     //Get all UPCs for this project with facing initialized to 0
		 Map<String,String> projectUpcFacingMap = getProjectUpcFacingMap(projectId);
		 //Get facing count for each UPC per store for ths project
		 Map<String,Map<String,String>> upcFacingPerStoreMap = getUpcFacingPerStoreMap(projectId);
		 
		 String sql = "SELECT result.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip, "
		 		+ " result.resultCode, result.countDistinctUpc, result.sumFacing, result.sumUpcConfidence, result.status, result.imageURL, result.agentId, result.taskId, DATE_FORMAT(result.processedDate,'%m/%d/%Y') as processedDate, "
		 		+ " result.visitDateId, result.imageUUID, result.linearFootage, result.countMissingUpc, result.percentageOsa, psgc.criteriaName, psgc.resultColor, result.resultComment, pwc.waveName "
		 		+ " FROM ProjectStoreResult result " 
		 		+ "	LEFT JOIN StoreMaster store ON result.storeId = store.storeId"
		 		+ " LEFT JOIN ProjectStoreGradingCriteria psgc ON result.projectId = psgc.projectId AND result.resultCode = psgc.resultCode"
		 		+ " LEFT JOIN ProjectWaveConfig pwc ON result.projectId = pwc.projectId AND result.waveId = pwc.waveId" 
		 		+ "	WHERE result.projectId=? and result.status = '1'" 
		 		+ " GROUP BY result.storeId, result.taskId,result.resultCode"
		 		+ "	ORDER BY result.waveId asc, result.visitDateId asc";
		 
	        Connection conn = null;

	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement ps = conn.prepareStatement(sql);
	            ps.setInt(1, projectId);

	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	                String storeId = rs.getString("storeId");
	                map.put("storeId", storeId);
	                map.put("retailerStoreId", rs.getString("retailerStoreId"));
	                map.put("retailerChainCode", rs.getString("retailerChainCode"));
	                map.put("retailer", rs.getString("retailer"));
	                map.put("street", ifNullToEmpty(rs.getString("street")));
	                map.put("city", rs.getString("city"));
	                map.put("stateCode", rs.getString("stateCode"));
	                map.put("state", rs.getString("state"));
	                map.put("zip", rs.getString("zip"));
	                map.put("agentId", rs.getString("agentId"));
	                String taskId=rs.getString("taskId");
	                map.put("taskId", taskId );
	                String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
	                if ( !visitDate.isEmpty() ) {
	                	try {
							visitDate = outSdf.format(inSdf.parse(visitDate));
						} catch (ParseException e) {
                            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
						}
	                }
	                map.put("visitDate", visitDate);
	                map.put("processedDate", ifNullToEmpty(rs.getString("processedDate")));
	                map.put("resultCode", rs.getString("resultCode"));
	                map.put("result", ifNullToEmpty(rs.getString("criteriaName")));
	                map.put("resultComment", ifNullToEmpty(rs.getString("resultComment")));
	                String imageUrl = rs.getString("imageURL");
	                if (StringUtils.isBlank(imageUrl)) {
	                	imageUrl = "Not Available";
	                }
	                map.put("imageURL", imageUrl );
	                map.put("countDistinctUpc", String.valueOf(rs.getInt("countDistinctUpc")));
	                map.put("sumFacing", String.valueOf(rs.getInt("sumFacing")));
	                map.put("sumUpcConfidence", String.valueOf(rs.getBigDecimal("sumUpcConfidence")));
	                map.put("status", rs.getString("status"));
	                map.put("waveName", ifNullToEmpty(rs.getString("waveName")));
	                map.putAll(projectUpcFacingMap);
	                if ( upcFacingPerStoreMap.containsKey(storeId+"|"+taskId)){
		                map.putAll(upcFacingPerStoreMap.get(storeId+"|"+taskId));
	                }
	                result.add(map);
	            }
	            rs.close();
	            ps.close();
	            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreResultsDetail----------------\n");
	            return result;
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

	private Map<String, Map<String, String>> getUpcFacingPerStoreMap(int projectId) {
		String upcForProjectSql = "select concat(t1.storeId,'|',t1.taskId) as storeTask,t1.upc,t1.facing from ProjectStoreData t1, (select upc from ProjectUpc where projectId=?) t2 "
				+ "	where t1.projectId=? and t1.upc != \"999999999999\" and t1.upc = t2.upc order by t1.storeId, t1.taskId";
		Map<String,Map<String,String>> upcFacingPerStoreMap = new LinkedHashMap<String,Map<String,String>>();
		 Connection conn = null;
		 try {
			 conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(upcForProjectSql);
	         ps.setInt(1, projectId);
	         ps.setInt(2, projectId);
	         ResultSet rs = ps.executeQuery();
	         String previousStoreId = "dummyStoreId";
	         String storeId = null;
	         Map<String,String> upcFacingMap = new LinkedHashMap<String,String>();
	         while(rs.next()){
	        	 storeId = rs.getString("storeTask");
	        	 if (!storeId.equalsIgnoreCase(previousStoreId)){
	        		 if ( previousStoreId.equalsIgnoreCase("dummyStoreId")) {
	        			 previousStoreId = storeId;
	        		 }
	        		 upcFacingPerStoreMap.put(previousStoreId, upcFacingMap);
	        		 upcFacingMap = new LinkedHashMap<String,String>();
	        		 previousStoreId = storeId;
	        	 }
	    		 upcFacingMap.put(rs.getString("upc"), rs.getString("facing"));
	         }
	         if (storeId != null ) {
	        	 upcFacingPerStoreMap.put(storeId, upcFacingMap);
	         }
	         rs.close();
	         ps.close();
		 } catch (SQLException e){
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
		 return upcFacingPerStoreMap;
	}

	private Map<String, Map<String, String>> getUpcFacingPerImageUUID(int projectId) {
		String upcForProjectSql = "select t1.imageUUID,t1.upc,count(*) as facing from ImageAnalysisNew t1, "
				+ "(select upc from ProjectUpc where projectId=?) t2 "
				+ "where t1.projectId=? and t1.upc != \"999999999999\" and t1.upc = t2.upc "
				+ "group by t1.imageUUID,t1.upc";
		Map<String,Map<String,String>> upcFacingPerStoreMap = new LinkedHashMap<String,Map<String,String>>();
		 Connection conn = null;
		 try {
			 conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(upcForProjectSql);
	         ps.setInt(1, projectId);
	         ps.setInt(2, projectId);
	         ResultSet rs = ps.executeQuery();
	         String previousStoreId = "dummyUUID";
	         String storeId = null;
	         Map<String,String> upcFacingMap = new LinkedHashMap<String,String>();
	         while(rs.next()){
	        	 storeId = rs.getString("imageUUID");
	        	 if (!storeId.equalsIgnoreCase(previousStoreId)){
	        		 if ( previousStoreId.equalsIgnoreCase("dummyUUID")) {
	        			 previousStoreId = storeId;
	        		 }
	        		 upcFacingPerStoreMap.put(previousStoreId, upcFacingMap);
	        		 upcFacingMap = new LinkedHashMap<String,String>();
	        		 previousStoreId = storeId;
	        	 }
	    		 upcFacingMap.put(rs.getString("upc"), rs.getString("facing"));
	         }
	         if (storeId != null ) {
	        	 upcFacingPerStoreMap.put(storeId, upcFacingMap);
	         }
	         rs.close();
	         ps.close();
		 } catch (SQLException e){
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
		 return upcFacingPerStoreMap;
	}

	
	
	private Map<String, String> getProjectUpcFacingMap(int projectId) {
		 String upcForProjectSql = "select upc from ProjectUpc where projectId=? order by upc";
		 Map<String,String> upcForProjectMap = new LinkedHashMap<String,String>();
		 Connection conn = null;
		 try {
			 conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(upcForProjectSql);
	         ps.setInt(1, projectId);
	         ResultSet rs = ps.executeQuery();
	         while(rs.next()){
	        	 upcForProjectMap.put(rs.getString("upc"), "0");
	         }
	         rs.close();
	         ps.close();
		 } catch (SQLException e){
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
		 return upcForProjectMap;
     }

	@Override
	public void reprocessProjectByStore(int projectId, List<String> storeIdsToReprocess) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts reprocessProjectByStore ----------------\n");

		String deleteImageAnalysisSql = "DELETE FROM ImageAnalysisNew WHERE projectId=? AND storeId=?";
		String updateProjectStoreResultsSql = "UPDATE ProjectStoreResult SET resultCode = '99' , status = '0' WHERE projectId=? AND storeId=?";
		String deleteProjectStoreDataSql = "DELETE FROM ProjectStoreData WHERE projectId=? AND storeId=?";
		String deleteProjectStoreScoreSql = "DELETE FROM ProjectStoreScore WHERE projectId=? AND storeId=?";
		String deleteProjectComponentScoreSql = "DELETE FROM ProjectStoreComponentScore WHERE projectId=? AND storeId=?";
		String deleteProjectComponentCriteriaScoreSql = "DELETE FROM ProjectStoreComponentCriteriaScore WHERE projectId=? AND storeId=?";
		String updateImageStatusSql = "UPDATE ImageStoreNew SET imageStatus=?, resultUploaded='0',"
				+ " imageResultComments = null, oldComments = null, objectiveResultStatus = null, imageReviewStatus = 0, imageResultCode = null"
				+ " WHERE projectId=? AND storeId=?";
		
		Connection conn = null;
		PreparedStatement deleteImageAnalysisPs = null;
	    PreparedStatement updateProjectStoreResultsPs = null;
	    PreparedStatement deleteProjectStoreDataPs = null;
	    PreparedStatement updateImageStatusPs = null;
	    PreparedStatement deleteProjectStoreScorePs = null;
	    PreparedStatement deleteProjectComponentScorePs = null;
	    PreparedStatement deleteProjectComponentCriteriaScorePs = null;
		
	    try {
	        conn = dataSource.getConnection();
	        conn.setAutoCommit(false);
	        
	        deleteImageAnalysisPs = conn.prepareStatement(deleteImageAnalysisSql);
	        updateProjectStoreResultsPs = conn.prepareStatement(updateProjectStoreResultsSql);
	        deleteProjectStoreDataPs = conn.prepareStatement(deleteProjectStoreDataSql);
	        updateImageStatusPs = conn.prepareStatement(updateImageStatusSql);
	        deleteProjectStoreScorePs = conn.prepareStatement(deleteProjectStoreScoreSql);
	        deleteProjectComponentScorePs = conn.prepareStatement(deleteProjectComponentScoreSql);
	        deleteProjectComponentCriteriaScorePs = conn.prepareStatement(deleteProjectComponentCriteriaScoreSql);
	        
	        for ( String storeId : storeIdsToReprocess ) {
	        	deleteImageAnalysisPs.setInt(1, projectId);
		        deleteImageAnalysisPs.setString(2, storeId);
		        deleteImageAnalysisPs.addBatch();
		        
		        updateProjectStoreResultsPs.setInt(1, projectId);
		        updateProjectStoreResultsPs.setString(2, storeId);
		        updateProjectStoreResultsPs.addBatch();
		        
		        deleteProjectStoreDataPs.setInt(1, projectId);
		        deleteProjectStoreDataPs.setString(2, storeId);
		        deleteProjectStoreDataPs.addBatch();
		        
		        deleteProjectStoreScorePs.setInt(1, projectId);
		        deleteProjectStoreScorePs.setString(2, storeId);
		        deleteProjectStoreScorePs.addBatch();
		        
		        deleteProjectComponentScorePs.setInt(1, projectId);
		        deleteProjectComponentScorePs.setString(2, storeId);
		        deleteProjectComponentScorePs.addBatch();
		        
		        deleteProjectComponentCriteriaScorePs.setInt(1, projectId);
		        deleteProjectComponentCriteriaScorePs.setString(2, storeId);
		        deleteProjectComponentCriteriaScorePs.addBatch();
		        
		        updateImageStatusPs.setString(1, "cron");
		        updateImageStatusPs.setInt(2, projectId);
		        updateImageStatusPs.setString(3, storeId);
		        updateImageStatusPs.addBatch();
	        }
	        
	        deleteImageAnalysisPs.executeBatch();
	        updateProjectStoreResultsPs.executeBatch();
	        deleteProjectStoreDataPs.executeBatch();
	        deleteProjectStoreScorePs.executeBatch();
	        deleteProjectComponentScorePs.executeBatch();
	        deleteProjectComponentCriteriaScorePs.executeBatch();
	        updateImageStatusPs.executeBatch();
	        
	        conn.commit();
	        
	     } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	         throw new RuntimeException(e);
	     } finally {
	         if (deleteImageAnalysisPs != null) {
	           try {   
	        	   deleteImageAnalysisPs.close();
                } catch (SQLException e) {
                   LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	            }
	         }
	         if (updateProjectStoreResultsPs != null) {
		           try {   
		        	   updateProjectStoreResultsPs.close();
	                } catch (SQLException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (deleteProjectStoreDataPs != null) {
		           try {   
		        	   deleteProjectStoreDataPs.close();
	                } catch (SQLException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (updateImageStatusPs != null) {
		           try {   
		        	   updateImageStatusPs.close();
	                } catch (SQLException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (deleteProjectStoreScorePs != null) {
		           try {   
		        	   deleteProjectStoreScorePs.close();
	                } catch (SQLException e) {
                     LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (deleteProjectComponentScorePs != null) {
		           try {   
		        	   deleteProjectComponentScorePs.close();
	                } catch (SQLException e) {
                     LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (deleteProjectComponentCriteriaScorePs != null) {
		           try {   
		        	   updateImageStatusPs.close();
	                } catch (SQLException e) {
                     LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         if (conn != null) {
		           try {
                         conn.setAutoCommit(true);
		            	 conn.close();
	                } catch (SQLException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         LOGGER.info("---------------ProcessImageDaoImpl Ends reprocessProjectByStore ----------------\n");
	      }
	}

	@Override
	public List<String> getProjectStoreIds(int projectId, boolean onlyDone) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreIds ----------------\n");
		List<String> storeIdsForProject = new ArrayList<String>();
		StringBuilder getAllStoresForProjectSqlBuilder = new StringBuilder("SELECT DISTINCT(storeId) FROM ImageStoreNew WHERE projectId=?");
		if (onlyDone) {
			getAllStoresForProjectSqlBuilder.append(" AND imageStatus in (\"done\",\"error\")");
		}
		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(getAllStoresForProjectSqlBuilder.toString());
	        ps.setInt(1, projectId);
	        ResultSet rs = ps.executeQuery();
	        
	        while (rs.next()) {
	        	storeIdsForProject.add(rs.getString("storeId"));
	        }
	        rs.close();
	        ps.close();
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
	         LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreIds ----------------\n");
	      }
		return storeIdsForProject;
	}

	@Override
	public void updateProjectResultStatus(List<StoreVisitResult> storeVisitResults) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateProjectResultStatus ----------------\n");

		String updateResultCodeSql = "UPDATE ProjectStoreResult SET resultCode=?, status=?, linearFootage=?, percentageOsa=?"
				+ " WHERE projectId=? AND storeId=? AND taskId=?";
		
		Connection conn = null;
		PreparedStatement updateResultCodePs = null;
		
	    try {
	        conn = dataSource.getConnection();
	        conn.setAutoCommit(false);
	        
	        updateResultCodePs = conn.prepareStatement(updateResultCodeSql);
	        
	        for ( StoreVisitResult result : storeVisitResults ) {
	        	updateResultCodePs.setString(1, result.getResultCode());
	        	updateResultCodePs.setString(2, result.getStatus());
	        	updateResultCodePs.setString(3, result.getLinearFootage());
	        	updateResultCodePs.setString(4, result.getPercentageOsa());
	        	updateResultCodePs.setInt(5, Integer.valueOf(result.getProjectId()));
	        	updateResultCodePs.setString(6, result.getStoreId());
	        	updateResultCodePs.setString(7, result.getTaskId());
	        	updateResultCodePs.addBatch();
	        }
	        
	        updateResultCodePs.executeBatch();
	        
	        conn.commit();
	        
	     } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw new RuntimeException(e);
	     } finally {
	         if (updateResultCodePs != null) {
	           try {   
	        	   updateResultCodePs.close();
                } catch (SQLException e) {
                   LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	            }
	         }
	         if (conn != null) {
		           try {
                         conn.setAutoCommit(true);
		            	 conn.close();
	                } catch (SQLException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
		            }
		         }
	         LOGGER.info("---------------ProcessImageDaoImpl Ends updateProjectResultStatus ----------------\n");
	      }
		
	}

	@Override
    public List<StoreVisit> getStoreVisitsWithImages(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreVisitsWithImages::projectId= {}",projectId);
        String sql = "SELECT storeId,taskId FROM ImageStoreNew WHERE projectId = ? group by storeId,taskId";
        List<StoreVisit> storeVisitList=new ArrayList<StoreVisit>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                StoreVisit storeVisit =new StoreVisit(rs.getString("storeId"),rs.getString("taskId"));
                storeVisitList.add(storeVisit);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreVisitsWithImages number Of Store and visit combination = {}",storeVisitList.size());

            return storeVisitList;
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
    public void saveStoreVisitRepResponses(int projectId,String storeId, String taskId, Map<String, String> repResponses) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts saveRepResponses::responses={}, storeId = {}, projectId = {}", repResponses, storeId, projectId );
        String deleteSql = "DELETE FROM ProjectRepResponses WHERE projectId = ? AND storeId = ? AND questionId = ? and taskId = ? ";
        String sql = "INSERT INTO ProjectRepResponses ( projectId, storeId, questionId, repResponse, taskId) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;

        if (repResponses != null && !repResponses.isEmpty()) {
            try {
                conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                PreparedStatement ps = conn.prepareStatement(sql);
                PreparedStatement deletePs = conn.prepareStatement(deleteSql);

                for( String questionId : repResponses.keySet()) {
                    ps.setInt(1, projectId);
                    ps.setString(2, storeId);
                    ps.setInt(3, Integer.parseInt(questionId));
                    ps.setString(4, repResponses.get(questionId));
                    ps.setString(5, taskId);
                    ps.addBatch();

                    deletePs.setInt(1, projectId);
                    deletePs.setString(2, storeId);
                    deletePs.setInt(3, Integer.parseInt(questionId));
                    deletePs.setString(4, taskId);
                    deletePs.addBatch();

                }
                //First Delete existing records
                deletePs.executeBatch();
                //Then Insert new records
                ps.executeBatch();

                conn.commit();
                ps.close();
                deletePs.close();

                LOGGER.info("---------------ProcessImageDaoImpl Ends saveRepResponses----------------\n");

            } catch (SQLException e) {
                LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            } finally {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close();
                    } catch (SQLException e) {
                        LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                    }
                }
            }
        }
    }

	@Override
	public Map<String,String> getRepResponsesByStoreVisit(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getRepResponsesByStoreVisit:: storeId ={}, projectId ={}, taskId={}", storeId, projectId, taskId);
        String sql = "SELECT * FROM ProjectRepResponses WHERE projectId = ? AND storeId = ? And taskId=?";
        Connection conn = null;
        Map<String,String> repResponses = new LinkedHashMap<String,String>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                repResponses.put(""+rs.getInt("questionId"), rs.getString("repResponse"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getRepResponsesByStoreVisit numberOfResponses = {}", repResponses.size());

            return repResponses;
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

	
	public List<ProjectQuestionObjective> getProjectQuestionObjectives(int projectId,String questionId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectQuestionObjectives:: ProjectId = {}, questionId={}", projectId,questionId);
        String sql = "SELECT * FROM ProjectObjectives  WHERE projectId = ? AND questionId = ? order by objectiveId";
        Connection conn = null;
        List<ProjectQuestionObjective> questionObjectives = new ArrayList<ProjectQuestionObjective>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, questionId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ProjectQuestionObjective objective = new ProjectQuestionObjective();
                objective.setProjectId(projectId);
//                objective.setCustomerProjectId(customerProjectId);
                objective.setQuestionId(questionId);
                objective.setObjectiveId(String.valueOf(rs.getInt("objectiveId")));
                objective.setObjectiveDesc(rs.getString("objectiveDesc"));
                objective.setObjectiveType(String.valueOf(rs.getInt("objectiveType")));
                objective.setObjectiveMetAndPresentCriteria(rs.getString("objectiveMetAndPresentCriteria"));
                objective.setObjectiveMetCriteria(rs.getString("objectiveMetCriteria"));
                objective.setObjectiveFalsifiedCriteria(rs.getString("objectiveFalsifiedCriteria"));
                objective.setObjectiveMismatchCriteria(rs.getString("objectiveMismatchCriteria"));
                objective.setObjectiveMetAndPresentComment(rs.getString("objectiveMetAndPresentComment"));
                objective.setObjectiveMetComment(rs.getString("objectiveMetComment"));
                objective.setObjectiveFalsifiedComment(rs.getString("objectiveFalsifiedComment"));
                objective.setObjectiveMismatchComment(rs.getString("objectiveMismatchComment"));
                objective.setObjectiveNotPresentCriteria(rs.getString("objectiveNotPresentCriteria"));
                objective.setObjectiveNotPresentComment(rs.getString("objectiveNotPresentComment"));
                questionObjectives.add(objective);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectQuestionObjectives numberOfObjectives = {}",questionObjectives.size());

            return questionObjectives;
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
	public List<StoreVisit> getStoreVisitsForRecompute(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreVisitsForRecompute::projectId={}",projectId);
		List<StoreVisit> storeVisits = new ArrayList<StoreVisit>();
		String sql = "SELECT storeId,taskId FROM ProjectStoreResult WHERE projectId=? AND resultCode NOT IN ('0','99') group by storeId,taskId";

		Connection conn = null;
	    try {
	        conn = dataSource.getConnection();
	        PreparedStatement ps = conn.prepareStatement(sql);
	        ps.setInt(1, projectId);
	        ResultSet rs = ps.executeQuery();

	        while (rs.next()) {
	        	storeVisits.add(new StoreVisit(rs.getString("storeId"),rs.getString("taskId")));
	        }
	        rs.close();
	        ps.close();
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
	         LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreVisitsForRecompute ----------------\n");
	      }
		return storeVisits;
	}

    @Override
    public List<StoreVisit> getAlreadyUploadedStoreVisit(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getAlreadyUploadedStoreVisit ----------------\n");
        List<StoreVisit> storeIdsForProject = new ArrayList<StoreVisit>();
        StringBuilder getAllStoresForRecomputeSqlBuilder = new StringBuilder("SELECT storeId,taskId FROM ProjectStoreResult WHERE projectId=? group by storeId,taskId");

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(getAllStoresForRecomputeSqlBuilder.toString());
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                StoreVisit storeVisit= new StoreVisit(rs.getString("storeId"),rs.getString("taskId"));
                storeIdsForProject.add(storeVisit);
            }
            rs.close();
            ps.close();
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
            LOGGER.info("---------------ProcessImageDaoImpl Ends getAlreadyUploadedStoreVisit ----------------\n");
        }
        return storeIdsForProject;
    }
	
	@Override
	public List<LinkedHashMap<String, String>> getProjectStoreResults(int projectId, String storeId, String month, String taskId) {
		 LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreResults::projectId={}, storeId={}, month={}", projectId, storeId, month);

		 String sql = "SELECT result.projectId, result.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip," +
	        		"    result.resultCode, psgc.criteriaName, result.countDistinctUpc, result.sumFacing, result.sumUpcConfidence, result.status, result.agentId, result.taskId, result.processedDate," + 
	        		"    result.visitDateId, result.linearFootage, result.countMissingUpc, result.percentageOsa, result.resultComment, result.waveId, waveConfig.waveName, count(distinct(aggData.brandName)) as countDistinctBrands " + 
	        		"	FROM ProjectStoreResult result" + 
	        		"	LEFT JOIN StoreMaster store ON result.storeId = store.storeId" +
	        		"	LEFT JOIN ProjectStoreGradingCriteria psgc ON result.projectId = psgc.projectId AND result.resultCode = psgc.resultCode" +
	        		"	LEFT JOIN ProjectWaveConfig waveConfig ON result.projectId = waveConfig.projectId AND COALESCE(result.waveId,'') = waveConfig.waveId" +
	        		"	LEFT JOIN (SELECT * FROM ProjectStoreData WHERE projectId = ? AND storeId = ? AND skuTypeId IN ('1','2','3')) aggData ON result.storeId = aggData.storeId AND result.projectId = aggData.projectId AND result.taskId = aggData.taskId" +
	        		"	WHERE result.projectId = ? and result.storeId = ? ";

		 if(null != taskId && !taskId.equalsIgnoreCase("-9") && !taskId.isEmpty()){
		     sql += " and result.taskId = ?" +
                     " group by result.storeId, result.taskId,result.resultCode" +
                     " order by result.visitDateId desc, result.taskId desc;";
         } else {
		     sql += " and result.visitDateId like ? group by result.storeId, result.taskId,result.resultCode" +
                     " order by result.visitDateId desc, result.taskId desc;";
         }

		 Connection conn = null;
		 List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();

		 SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY");
		 SimpleDateFormat visitDateSdf = new SimpleDateFormat("yyyyMMdd");
	        
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setInt(3, projectId);
            ps.setString(4, storeId);

            if(null != taskId && !taskId.equalsIgnoreCase("-9") && !taskId.isEmpty()){
                ps.setString(5, taskId);
            } else {
                ps.setString(5, ifNullToEmpty(month)+'%');
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();

                map.put("projectId", rs.getInt("projectId")+"");
                map.put("storeId", rs.getString("storeId"));
                map.put("retailerStoreId", rs.getString("retailerStoreId"));
                map.put("retailerChainCode", rs.getString("retailerChainCode"));
                map.put("retailer", rs.getString("retailer"));
                map.put("street", rs.getString("street"));
                map.put("city", rs.getString("city"));
                map.put("stateCode", rs.getString("stateCode"));
                map.put("state", rs.getString("state"));
                map.put("zip", rs.getString("zip"));
                map.put("resultCode", rs.getString("resultCode"));
                map.put("result", rs.getString("criteriaName"));
                map.put("resultComment", rs.getString("resultComment"));
                map.put("countDistinctUpc", String.valueOf(rs.getInt("countDistinctUpc")));
                map.put("countDistinctBrands", String.valueOf(rs.getInt("countDistinctBrands")));
                map.put("sumFacing", String.valueOf(rs.getInt("sumFacing")));
                map.put("sumUpcConfidence", String.valueOf(rs.getBigDecimal("sumUpcConfidence")));
                map.put("status", rs.getString("status"));
                map.put("agentId", rs.getString("agentId"));
                map.put("taskId", rs.getString("taskId"));
                map.put("visitDateId", sdf.format(visitDateSdf.parse(rs.getString("visitDateId"))) );
                map.put("linearFootage", rs.getString("linearFootage"));
                map.put("countMissingUpc", rs.getString("countMissingUpc"));
                map.put("percentageOsa", rs.getString("percentageOsa"));
                map.put("waveId", ifNullToEmpty(rs.getString("waveId")));
                map.put("waveName", ifNullToEmpty(rs.getString("waveName")));
                
                Timestamp processedDate = rs.getTimestamp("processedDate");

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(processedDate.getTime());

                String processedDateStr = sdf.format(cal.getTime());
                map.put("processedDate", processedDateStr);

                result.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreResults----------------\n");

            return result;
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
	public Map<String, String> generateImageResult(ImageStore image, List<ImageAnalysis> imageAnalysisOutput ) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts generateImageResult::projectId={}::storeId={}::taskId={}::questionId={}",image.getProjectId(),image.getStoreId(),image.getTaskId(),image.getQuestionId());
		
		Map<String,String> resultMap = new HashMap<String,String>();
		
		ImageResultCode resultCode;
		
		// get UPC-facing list from imageAnalylsis output
		Map<String,Map<String,String>> imageAnalysisUPCFacingMap = new LinkedHashMap<String,Map<String,String>>();
		for( ImageAnalysis analysis : imageAnalysisOutput) {
			Map<String,String> facingMap =(Map<String,String>) imageAnalysisUPCFacingMap.get(analysis.getUpc());
			if ( facingMap == null ) {
				facingMap = new HashMap<String,String>();
				facingMap.put("facing", "1");
				imageAnalysisUPCFacingMap.put(analysis.getUpc(), facingMap);
			} else {
				facingMap.put("facing", ""+ (Integer.parseInt(facingMap.get("facing")) + 1) ) ;
			}
			imageAnalysisUPCFacingMap.put(analysis.getUpc(), facingMap);
		}
		
		Map<String,Object> wrappedImageAnalysisUPCFacingMap = new HashMap<String,Object>();
		wrappedImageAnalysisUPCFacingMap.put("UPCs", imageAnalysisUPCFacingMap);
		LOGGER.info("---------------ProcessImageDaoImpl--generateImageResult::UPC-Facings detected :: {}",imageAnalysisUPCFacingMap);
		
		List<String> detectedUPCs = new ArrayList<String>(imageAnalysisUPCFacingMap.keySet());
		if ( detectedUPCs.isEmpty() ) {
			resultCode = ImageResultCode.REJECT_INSUFFICIENT;
			resultMap.put("resultCode", resultCode.getCode());
			resultMap.put("resultComment", "Incorrect photo");
			resultMap.put("objectiveResultStatus", "Objectives Not Evaluated");
		} else {
			Map<String,String> repResponses = getRepResponsesByStoreVisit(image.getProjectId(), image.getStoreId(), image.getTaskId());
			LOGGER.info("---------------ProcessImageDaoImpl--generateImageResult::Rep Responses :: {}", repResponses );
			
			List<ProjectQuestionObjective> projectQuestionObjectives = getProjectQuestionObjectives(image.getProjectId(), image.getQuestionId());
			LOGGER.info("---------------ProcessImageDaoImpl--generateImageResult::Project Objectives :: {}", projectQuestionObjectives );
			
			//calculate resultCode based on UPC detections
			resultMap = calculateImageResult(detectedUPCs, projectQuestionObjectives, repResponses, wrappedImageAnalysisUPCFacingMap);
			LOGGER.info("---------------ProcessImageDaoImpl--generateImageResult::ImageResult Result :: {}", resultMap );
		}
		
		return resultMap;
	}

	private Map<String,String> calculateImageResult(List<String> detectedUPCs, List<ProjectQuestionObjective> projectQuestionObjectives, Map<String, String> repResponses, Map<String, Object> imageAnalysisUPCFacingMap) {
		Map<String,String> returnMap = new HashMap<String,String>();
		
		Map<String,List<String>> objectiveResultMap = new LinkedHashMap<String, List<String>>();
		objectiveResultMap.put("MET&PRESENT", new ArrayList<String>()); //MET&PRESENT
		objectiveResultMap.put("NOTPRESENT", new ArrayList<String>()); //NOTPRESENT
		objectiveResultMap.put("MET", new ArrayList<String>()); //MET
		objectiveResultMap.put("FALSIFIED", new ArrayList<String>()); //FALSIFIED
		objectiveResultMap.put("MISMATCH", new ArrayList<String>()); //MISMATCH
		objectiveResultMap.put("NOTA", new ArrayList<String>()); //NONE OF THE ABOVE
		
		Map<String,List<String>> type1ObjectiveResultMap = new LinkedHashMap<String, List<String>>();
		type1ObjectiveResultMap.put("MET&PRESENT", new ArrayList<String>()); //MET&PRESENT
		type1ObjectiveResultMap.put("NOTPRESENT", new ArrayList<String>()); //NOTPRESENT
		type1ObjectiveResultMap.put("MET", new ArrayList<String>()); //MET
		type1ObjectiveResultMap.put("FALSIFIED", new ArrayList<String>()); //FALSIFIED
		type1ObjectiveResultMap.put("MISMATCH", new ArrayList<String>()); //MISMATCH
		type1ObjectiveResultMap.put("NOTA", new ArrayList<String>()); //NONE OF THE ABOVE
		
		List<ProjectQuestionObjective> type1Objectives = new ArrayList<ProjectQuestionObjective>();
		
		Set<String> imageResultComments = new LinkedHashSet<String>();
		Set<String> type1imageResultComments = new LinkedHashSet<String>();
		
		StringBuilder objectiveResultStatusBuilder = new StringBuilder();
		StringBuilder type1ObjectiveResultStatusBuilder = new StringBuilder();
		
		final String SEPARATOR_COMMA = ", ";
		final String SEPARATOR_HYPHEN = "-";
		final String countDistinctUpc = ""; //dummy value for expressionevaluator argument
		final String percentageOsa = ""; //dummy value for expressionevaluator argument
		final String hasLowQualityImages = ""; //dummy value for expressionevaluator argument
		final String hasLowConfidenceDetections = ""; //dummy value for expressionevaluator argument
		final String distributionPercentage = ""; //dummy value for expressionevaluator argument
		
		for( ProjectQuestionObjective objective : projectQuestionObjectives ) {
			//Keep a list of type1 objectives
			boolean isType1Objective = "1".equals(objective.getObjectiveType());
			if ( isType1Objective ) {
				type1Objectives.add(objective);
			}
			
			//First evaluate MET&PRESENT Criteria
			if ( StringUtils.isNotBlank(objective.getObjectiveMetAndPresentCriteria()) ) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: evaluating MET&PRESENT Criteria :: {}", objective.getObjectiveMetAndPresentCriteria() );
				boolean result = ExpressionEvaluator.evaluate(objective.getObjectiveMetAndPresentCriteria(), detectedUPCs, imageAnalysisUPCFacingMap, repResponses, countDistinctUpc, 
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
				if ( result ) {
					objectiveResultMap.get("MET&PRESENT").add(objective.getQuestionId());
					if ( isType1Objective ) {
						type1ObjectiveResultMap.get("MET&PRESENT").add(objective.getQuestionId());
						type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MET&PRESENT").append(SEPARATOR_COMMA);
					} else {
						objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MET&PRESENT").append(SEPARATOR_COMMA);
					}
					if ( StringUtils.isNotBlank(objective.getObjectiveMetAndPresentComment())) {
						imageResultComments.add( objective.getObjectiveMetAndPresentComment());
						if ( isType1Objective ) {
							type1imageResultComments.add( objective.getObjectiveMetAndPresentComment());
						}
					}
					continue;
				}
			}
			
			//Then evaluate NOTPRESENT Criteria
			if ( StringUtils.isNotBlank(objective.getObjectiveNotPresentCriteria()) ) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: evaluating NOTPRESENT Criteria :: {}", objective.getObjectiveNotPresentCriteria());
				boolean result = ExpressionEvaluator.evaluate(objective.getObjectiveNotPresentCriteria(), detectedUPCs, imageAnalysisUPCFacingMap, repResponses, countDistinctUpc, 
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
				if ( result ) {
					objectiveResultMap.get("NOTPRESENT").add(objective.getQuestionId());
					if ( isType1Objective ) {
						type1ObjectiveResultMap.get("NOTPRESENT").add(objective.getQuestionId());
						type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("NOTPRESENT").append(SEPARATOR_COMMA);	
					} else {
						objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("NOTPRESENT").append(SEPARATOR_COMMA);
					}
					if ( StringUtils.isNotBlank(objective.getObjectiveNotPresentComment())) {
						imageResultComments.add( objective.getObjectiveNotPresentComment());
						if ( isType1Objective ) {
							type1imageResultComments.add( objective.getObjectiveNotPresentComment());
						}
					}
					continue;
				}
			}
			
			//Then evaluate MET Criteria
			if ( StringUtils.isNotBlank(objective.getObjectiveMetCriteria()) ) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: evaluating MET Criteria :: {}", objective.getObjectiveMetCriteria());
				boolean result = ExpressionEvaluator.evaluate(objective.getObjectiveMetCriteria(), detectedUPCs, imageAnalysisUPCFacingMap, repResponses, countDistinctUpc,
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
				if ( result ) {
					objectiveResultMap.get("MET").add(objective.getQuestionId());
					if ( isType1Objective ) {
						type1ObjectiveResultMap.get("MET").add(objective.getQuestionId());
						type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MET").append(SEPARATOR_COMMA);
					} else {
						objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MET").append(SEPARATOR_COMMA);
					}
					if ( StringUtils.isNotBlank(objective.getObjectiveMetComment())) {
						imageResultComments.add( objective.getObjectiveMetComment());
						if ( isType1Objective ) {
							type1imageResultComments.add( objective.getObjectiveMetComment());
						}
					}
					continue;
				}
			}
			
			//Then evaluate FALSIFICATION Criteria
			if ( StringUtils.isNotBlank(objective.getObjectiveFalsifiedCriteria())) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: evaluating FALSIFICATION Criteria :: {}",objective.getObjectiveFalsifiedCriteria());
				boolean result = ExpressionEvaluator.evaluate(objective.getObjectiveFalsifiedCriteria(), detectedUPCs, imageAnalysisUPCFacingMap, repResponses, countDistinctUpc,
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
				if ( result ) {
					objectiveResultMap.get("FALSIFIED").add(objective.getQuestionId());
					if ( isType1Objective ) {
						type1ObjectiveResultMap.get("FALSIFIED").add(objective.getQuestionId());
						type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("FALSIFIED").append(SEPARATOR_COMMA);
					} else {
						objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("FALSIFIED").append(SEPARATOR_COMMA);
					}
					if ( StringUtils.isNotBlank(objective.getObjectiveFalsifiedComment())) {
						imageResultComments.add( objective.getObjectiveFalsifiedComment());
						if ( isType1Objective ) {
							type1imageResultComments.add( objective.getObjectiveFalsifiedComment());
						}
					}
					continue;
				}
			}
			
			//Then evaluate MISMATCH Criteria
			if ( StringUtils.isNotBlank(objective.getObjectiveMismatchCriteria()) ) {
				LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: evaluating MISMATCH Criteria :: {}", objective.getObjectiveMismatchCriteria());
				boolean result = ExpressionEvaluator.evaluate(objective.getObjectiveMismatchCriteria(), detectedUPCs, imageAnalysisUPCFacingMap, repResponses, countDistinctUpc,
						percentageOsa, distributionPercentage, hasLowQualityImages, hasLowConfidenceDetections);
				if ( result ) {
					objectiveResultMap.get("MISMATCH").add(objective.getQuestionId());
					if ( isType1Objective ) {
						type1ObjectiveResultMap.get("MISMATCH").add(objective.getQuestionId());
						type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MISMATCH").append(SEPARATOR_COMMA);
					} else {
						objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
							.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("MISMATCH").append(SEPARATOR_COMMA);
					}
					if ( StringUtils.isNotBlank(objective.getObjectiveMismatchComment())) {
						imageResultComments.add( objective.getObjectiveMismatchComment());	
						if ( isType1Objective ) {
							type1imageResultComments.add( objective.getObjectiveMismatchComment());
						}
					}
					continue;
				}
			}

			//None are evaluated to true, set status as NOTA ..no comments for NOTA
			objectiveResultMap.get("NOTA").add(objective.getQuestionId());
			if ( isType1Objective ) {
				type1ObjectiveResultMap.get("NOTA").add(objective.getQuestionId());
				type1ObjectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
					.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("NOTA").append(SEPARATOR_COMMA);
			} else {
				objectiveResultStatusBuilder.append(objective.getQuestionId()).append(SEPARATOR_HYPHEN).append(objective.getObjectiveId()).append(SEPARATOR_HYPHEN)
					.append(objective.getObjectiveType()).append(SEPARATOR_HYPHEN).append("NOTA").append(SEPARATOR_COMMA);
			}
		}
		
		//remove null or empty comments from the comments set
		imageResultComments.remove(null);
		imageResultComments.remove("");
		type1imageResultComments.remove(null);
		type1imageResultComments.remove("");
		//extract comma separated string representation of the comments set
		String imageResultComment = imageResultComments.toString().replaceAll("\\[", "").replaceAll("\\]","");
		String type1imageResultComment = type1imageResultComments.toString().replaceAll("\\[", "").replaceAll("\\]","");

		String objectiveResultStatus = objectiveResultStatusBuilder.toString();
		if ( objectiveResultStatus.length() > SEPARATOR_COMMA.length()  ) {
			objectiveResultStatus = objectiveResultStatus.substring(0,objectiveResultStatus.length() - SEPARATOR_COMMA.length() ); //Remove last separator
		}
		String type1ObjectiveResultStatus = type1ObjectiveResultStatusBuilder.toString();
		if ( type1ObjectiveResultStatus.length() > SEPARATOR_COMMA.length()  ) {
			type1ObjectiveResultStatus = type1ObjectiveResultStatus.substring(0,type1ObjectiveResultStatus.length() - SEPARATOR_COMMA.length() ); //Remove last separator
		}
		
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Objective result for image :: {}", objectiveResultMap);
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Image Result Comment :: {}", imageResultComment );

		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Type 1 objective result for image :: {}" ,type1ObjectiveResultMap );
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Type 1 Image Result Comment :: {}" ,type1imageResultComment);
		
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Type1 Objective result status string :: {}", type1ObjectiveResultStatus );
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: Other Objective result status string :: {}", objectiveResultStatus );
		
		//Combine the result status so that type 1 objective results are stored first
		objectiveResultStatus = ifNullToEmpty(type1ObjectiveResultStatus) + SEPARATOR_COMMA + ifNullToEmpty(objectiveResultStatus);
		
		ImageResultCode resultCode;

		if ( objectiveResultMap.get("MET&PRESENT").size() == projectQuestionObjectives.size() ) { //If all objectives are met&present
			resultCode = ImageResultCode.APPROVED_EXCELLENT;
		} else if ( type1ObjectiveResultMap.get("MET&PRESENT").size() == type1Objectives.size() ) { //If all objectives of type 1 are met&present
			resultCode = ImageResultCode.APPROVE;
		} else if ( 
				(type1ObjectiveResultMap.get("MET&PRESENT").size() + 
						type1ObjectiveResultMap.get("MET").size() + 
						type1ObjectiveResultMap.get("MISMATCH").size() ) == type1Objectives.size()	) { //If all objectives of type 1 are met&present, met or mismatch
			resultCode = ImageResultCode.APPROVED_WITH_ISSUES;
		} else if (type1ObjectiveResultMap.get("FALSIFIED").size() == type1Objectives.size() ) { //If all type1 objectives are falsified
			resultCode = ImageResultCode.REJECT_LEVEL_3;
		} else if ( type1ObjectiveResultMap.get("FALSIFIED").size() >= 2 ) { // If Two or more type1 objectives is falsified
			resultCode = ImageResultCode.REJECT_LEVEL_1;
		} else if ( type1ObjectiveResultMap.get("NOTPRESENT").size() >= 1 && type1ObjectiveResultMap.get("FALSIFIED").size() == 0) { // If any objective is notpresent, and none are falsified
			resultCode = ImageResultCode.UNAPPROVED;
		} else { // Cover all.. any other condition.
			resultCode = ImageResultCode.APPROVED_PENDING_REVIEW;
		}
		
		LOGGER.info("---------------ProcessImageDaoImpl--calculateImageResult:: result evaluated for image :: {}" ,resultCode.getDesc());

		returnMap.put("resultCode", resultCode.getCode());
		if ( resultCode == ImageResultCode.UNAPPROVED ) {
			returnMap.put("resultComment", type1imageResultComment);
		} else {
			returnMap.put("resultComment", imageResultComment);
		}
		returnMap.put("objectiveResultStatus", objectiveResultStatus);
		
		return returnMap;
	}

	@Override
	public void updateImageResultCodeAndStatus(String imageUUID, String code, String comment, String imageReviewStatus, String objectiveResultStatus) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeAndStatus"
				+ "::imageResultCode={} ::imageResultComment={} ::imageReviewStatus={} ::objectiveResultStatus={}::imageUUID={}",code,comment,imageReviewStatus,objectiveResultStatus,imageUUID);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageResultCode = ?, oldComments = imageResultComments, imageResultComments = ?, imageReviewStatus = ?, objectiveResultStatus = ?, lastUpdatedTimestamp = ?, resultUploaded = '0', processedDate = NOW() WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, code);
            ps.setString(2, comment);
            ps.setInt(3, Integer.valueOf(imageReviewStatus));
            ps.setString(4, objectiveResultStatus);
            ps.setString(5, String.valueOf(currTimestamp));
            ps.setString(6, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeAndStatus----------------\n");
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
	
	@Override
	public void updateImageResultCodeBatch(Map<String,String> imageResultMap) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeBatch::imageResultMap={}",imageResultMap);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageResultCode = ?, oldComments = imageResultComments, imageResultComments = ?, imageStatus='done', resultUploaded='0', lastUpdatedTimestamp = ?, imageReviewStatus = 1 , processedDate = NOW() WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            
            for(Entry<String, String> entry : imageResultMap.entrySet() ) {
            	String resultCode, resultComment;
            	String value = entry.getValue();
            	String [] splitValues = value.split("#");
            	resultCode = splitValues[0];
            	if ( splitValues.length == 2 ){
            		resultComment = splitValues[1];
            	} else {
            		resultComment = "";
            	}
            	ps.setString(1, resultCode);
            	ps.setString(2, resultComment);
            	ps.setString(3, String.valueOf(currTimestamp));
                ps.setString(4, entry.getKey());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeBatch----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
	}

	@Override
	public List<LinkedHashMap<String, String>> getProjectAllStoreImageResults(int projectId, String status) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreImageResults::projectId={}, status={}", projectId,status);
	     List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
	     SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
	     outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	     SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	     inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

	     boolean includeImagesWithNullResultCode = false;
	     if ( StringUtils.isNotBlank(status) && status.equals("all") ) {
	    	 includeImagesWithNullResultCode = true;
	     }

	     //Get all UPCs for this project with facing initialized to 0
		 Map<String,String> projectUpcFacingMap = getProjectUpcFacingMap(projectId);
		 //Get facing count for each UPC per store for ths project
		 Map<String,Map<String,String>> upcFacingPerStoreMap = getUpcFacingPerImageUUID(projectId);

		 String sql = "SELECT image.imageUUID, image.resultUploaded, image.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip,"
		 		+ " image.origWidth, image.newWidth, image.origHeight, image.newHeight, image.agentId, image.taskId,  image.questionId, image.fileId, image.dateId, DATE_FORMAT(image.processedDate,'%m/%d/%Y') as processedDate,image.imageURL, image.imageStatus,image.imageResultCode,image.imageReviewStatus, image.imageResultComments, image.objectiveResultStatus "
		 		+ " FROM ImageStoreNew image, StoreMaster store"
		 		+ " WHERE image.projectId = ? and image.storeId = store.storeId and image.fileId is not null"
		 		+ " order by image.imageResultCode desc";

		 Connection conn = null;

	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement ps = conn.prepareStatement(sql);
	            ps.setInt(1, projectId);
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	            	String imageResultCode = rs.getString("imageResultCode");
	            	if ( StringUtils.isNotBlank(imageResultCode) || includeImagesWithNullResultCode) {
		            	LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
		            	map.put("imageResultCode", ifNullToEmpty(imageResultCode));
		                String imageUUID = rs.getString("imageUUID");
		                map.put("imageUUID", imageUUID);
		                map.put("resultUploaded", rs.getString("resultUploaded"));
		                map.put("storeId",  rs.getString("storeId"));
		                map.put("retailerStoreId", ifNullToEmpty(rs.getString("retailerStoreId")));
		                map.put("retailerChainCode", ifNullToEmpty(rs.getString("retailerChainCode")));
		                map.put("retailer", ifNullToEmpty(rs.getString("retailer")));
		                map.put("street", ifNullToEmpty(rs.getString("street")));
		                map.put("city", ifNullToEmpty(rs.getString("city")));
		                map.put("stateCode", ifNullToEmpty(rs.getString("stateCode")));
		                map.put("state", ifNullToEmpty(rs.getString("state")));
		                map.put("zip", ifNullToEmpty(rs.getString("zip")));
		                map.put("agentId", rs.getString("agentId"));
		                map.put("taskId", rs.getString("taskId"));
		                map.put("questionId", rs.getString("questionId"));
		                map.put("fileId", rs.getString("fileId"));
		                map.put("visitDateId", ifNullToEmpty(rs.getString("dateId")));
		                String visitDate = ifNullToEmpty(rs.getString("dateId"));
		                if ( !visitDate.isEmpty() ) {
		                	try {
								visitDate = outSdf.format(inSdf.parse(visitDate));
							} catch (ParseException e) {
                                LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
							}
		                }
		                map.put("visitDate", visitDate);
		                map.put("processedDate", ifNullToEmpty(rs.getString("processedDate")));
		                map.put("imageURL", ifNullToEmpty(rs.getString("imageURL")));
		                map.put("imageStatus", rs.getString("imageStatus"));
		                map.put("imageResultCode", rs.getString("imageResultCode"));
		                map.put("imageReviewStatus", rs.getString("imageReviewStatus"));

		                map.put("imageResultDesc", ImageResultCode.getImageResultCodeFromCode(imageResultCode).getDesc());

		                map.put("imageResultComments", ifNullToEmpty(rs.getString("imageResultComments")));

		                map.put("objectiveResultStatus", ifNullToEmpty(rs.getString("objectiveResultStatus")));

		                map.put("origWidth", rs.getString("origWidth"));
		                map.put("newWidth", rs.getString("newWidth"));
		                map.put("origHeight", rs.getString("origHeight"));
		                map.put("newHeight", rs.getString("newHeight"));

		                map.putAll(projectUpcFacingMap);
		                if ( upcFacingPerStoreMap.containsKey(imageUUID)){
			                map.putAll(upcFacingPerStoreMap.get(imageUUID));
		                }
		                result.add(map);
		            }
	            }
	            rs.close();
	            ps.close();
	            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreImageResults----------------\n");
	            return result;
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
	public LinkedHashMap<String,Object> getNextStoreVisitToReview(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getNextStoreVisitToReview::projectId={}", projectId);
	     LinkedHashMap<String,Object> result=new LinkedHashMap<String,Object>();
	     SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
	     outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	     SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	     inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));

		 String allStoreVisitPendingReviewSql = "SELECT storeId,taskId FROM ImageStoreNew \n" +
		 		"WHERE projectId=? AND imageReviewStatus='0' \n" +
		 		"    AND imageResultCode IS NOT NULL \n" +
		 		"GROUP BY storeId,taskId ORDER BY dateId ASC";

	     String oneStoreVisitDetailsSql = "SELECT image.imageUUID, image.resultUploaded, image.storeId,\n" +
	     		"	image.origWidth, image.newWidth, image.origHeight, image.newHeight, image.agentId, image.taskId, " +
	     		"   image.questionId, image.fileId, image.dateId, DATE_FORMAT(image.processedDate,'%m/%d/%Y') as processedDate, " +
	     		"   image.imageURL, image.imageStatus,image.imageResultCode,image.imageReviewStatus, image.imageResultComments, image.objectiveResultStatus \n" +
	     		" FROM ImageStoreNew image \n" +
	     		" WHERE image.projectId = ? and image.storeId=? and image.taskId=?\n" +
	     		"    AND image.fileId is not null\n" +
	     		" ORDER BY image.imageResultCode desc";

		 Connection conn = null;

	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement ps = conn.prepareStatement(allStoreVisitPendingReviewSql);
	            ps.setInt(1, projectId);
	            ResultSet rs = ps.executeQuery();

	            int storeCount=0;
	            String storeId = null, taskId = null;
	            while(rs.next()) {
	            	if (storeCount == 0 ) {
	            		storeId = rs.getString("storeId");
	            		taskId = rs.getString("taskId");
	            	}
	            	storeCount++;
	            }
	            rs.close();
	            ps.close();

	            if ( StringUtils.isNoneBlank(storeId, taskId) ) {
	            	result.put("projectId", projectId);
	            	result.put("storeId", storeId);
	            	result.put("taskId", taskId);
	            	result.put("imagesPendingReview", "1");
	            	result.put("totalStoresPendingReview", storeCount);

	            	PreparedStatement detailsPs = conn.prepareStatement(oneStoreVisitDetailsSql);
	            	detailsPs.setInt(1, projectId);
	            	detailsPs.setString(2, storeId);
	            	detailsPs.setString(3, taskId);
		            ResultSet detailsRs = detailsPs.executeQuery();

		            List<LinkedHashMap<String,String>> images = new ArrayList<LinkedHashMap<String,String>>();
		            while (detailsRs.next()) {
		            	LinkedHashMap<String,String> map = new LinkedHashMap<String,String>();
		            	String imageResultCode = detailsRs.getString("imageResultCode");
			            map.put("imageResultCode", ifNullToEmpty(imageResultCode));
			            String imageUUID = detailsRs.getString("imageUUID");
			            map.put("imageUUID", imageUUID);
			            map.put("resultUploaded", detailsRs.getString("resultUploaded"));
			            map.put("storeId",  detailsRs.getString("storeId"));
			            map.put("taskId", detailsRs.getString("taskId"));
			            map.put("questionId", detailsRs.getString("questionId"));
			            map.put("fileId", detailsRs.getString("fileId"));
			            map.put("visitDateId", ifNullToEmpty(detailsRs.getString("dateId")));
			            String visitDate = ifNullToEmpty(detailsRs.getString("dateId"));
			            //special - start
			            result.put("visitDate",visitDate);
			            //special - end
			            if ( !visitDate.isEmpty() ) {
			            	try {
			            		visitDate = outSdf.format(inSdf.parse(visitDate));
							} catch (ParseException e) {
								LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
							}
			            }
			            map.put("visitDate", visitDate);
			            map.put("processedDate", ifNullToEmpty(detailsRs.getString("processedDate")));
			            map.put("imageURL", ifNullToEmpty(detailsRs.getString("imageURL")));
			            map.put("imageStatus", detailsRs.getString("imageStatus"));
			            map.put("imageResultCode", detailsRs.getString("imageResultCode"));
			            map.put("imageReviewStatus", detailsRs.getString("imageReviewStatus"));

			            map.put("imageResultDesc", ImageResultCode.getImageResultCodeFromCode(imageResultCode).getDesc());

			            map.put("imageResultComments", ifNullToEmpty(detailsRs.getString("imageResultComments")));

			            map.put("objectiveResultStatus", ifNullToEmpty(detailsRs.getString("objectiveResultStatus")));

			            map.put("origWidth", detailsRs.getString("origWidth"));
			            map.put("newWidth", detailsRs.getString("newWidth"));
			            map.put("origHeight", detailsRs.getString("origHeight"));
			            map.put("newHeight", detailsRs.getString("newHeight"));
			            images.add(map);
		            }
		            result.put("images", images);
		            detailsRs.close();
		            detailsPs.close();
	            }

	            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreImageResults----------------\n");
	            return result;
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
	public Map<String, Map<String,String>> getAllImageResultsForStoresWithMultipleImages(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getAllImageResultsForStoresWithMultipleImages::projectId={}",projectId);

		String sql = "SELECT storeId,taskId,questionId,GROUP_CONCAT(imageUUID) AS imageUUIDs,GROUP_CONCAT(imageResultCode) AS resultCodes " +
				" FROM ImageStoreNew " +
				" WHERE projectId=? AND imageResultCode IS NOT NULL " +
				" GROUP BY storeId,taskId,questionId " +
				" HAVING COUNT(*) > 1 ";

		Map<String,Map<String,String>> imageResults = new LinkedHashMap<String,Map<String,String>>();
		Connection conn = null;
		 try {
			 conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ps.setInt(1, projectId);

	         ResultSet rs = ps.executeQuery();

	         while(rs.next()){
	        	 String storeTaskQuestion = rs.getString("storeId")+rs.getString("taskId")+rs.getString("questionId");
	        	 Map<String,String> imagesForAStoreTaskQuestion = new LinkedHashMap<String,String>();
	        	 String allImageUUIDs = rs.getString("imageUUIDs");
	        	 String allResultCodes = rs.getString("resultCodes");
	        	 String[] imageUUIDs = allImageUUIDs.split(",");
	        	 String[] resultCodes = allResultCodes.split(",");
	        	 for ( int i=0 ; i < imageUUIDs.length ; i++ ) {
		        	 imagesForAStoreTaskQuestion.put(imageUUIDs[i], resultCodes[i]);
	        	 }
	        	 imageResults.put(storeTaskQuestion, imagesForAStoreTaskQuestion);
	         }

	         rs.close();
	         ps.close();
	         LOGGER.info("---------------ProcessImageDaoImpl Ends getAllImageResultsForStoresWithMultipleImages----------------\n");
		 } catch (SQLException e){
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
		 return imageResults;
	}

	@Override
	public List<Map<String, String>> getAllImageResultsForDuplicateImages(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getAllImageResultsForDuplicateImages::projectId={}",projectId);
		String sql = "SELECT imageHashScore,GROUP_CONCAT(imageUUID) AS imageUUIDs,GROUP_CONCAT(ImageStoreNew.storeId) AS storeIds,GROUP_CONCAT(taskId) AS taskIds,GROUP_CONCAT(RetailerStoreID) AS retailerStoreIds" +
				" FROM ImageStoreNew, StoreMaster" +
				" WHERE projectId=? AND imageHashScore IS NOT NULL AND imageHashScore <> '0' AND imageResultCode IS NOT NULL AND ImageStoreNew.storeId = StoreMaster.StoreID" +
				" GROUP BY imageHashScore" +
				" HAVING COUNT(*) > 1";

		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		Connection conn = null;
		 try {
			 conn = dataSource.getConnection();
	         PreparedStatement ps = conn.prepareStatement(sql);
	         ps.setInt(1, projectId);

	         ResultSet rs = ps.executeQuery();
	         while(rs.next()){
	        	 String imageHashScore = rs.getString("imageHashScore");

	        	 String allImageUUIDs = rs.getString("imageUUIDs");
	        	 String allStoreIds = rs.getString("storeIds");
	        	 String allTaskIds = rs.getString("taskIds");
	        	 String allRetailerStoreIds = rs.getString("retailerStoreIds");
	        	 String[] imageUUIDs = allImageUUIDs.split(",");
	        	 String[] storeIds = allStoreIds.split(",");
	        	 String[] taskIds = allTaskIds.split(",");
	        	 String[] retailerStoreIds = allRetailerStoreIds.split(",");

	        	 for ( int i=0 ; i < imageUUIDs.length ; i++ ) {
		        	 Map<String,String> map = new LinkedHashMap<String, String>();
		        	 map.put("imageHashScore", imageHashScore);
		        	 map.put("imageUUID", imageUUIDs[i]);
		        	 map.put("storeId", storeIds[i]);
		        	 map.put("taskId", taskIds[i]);
		        	 map.put("retailerStoreId", retailerStoreIds[i]);
		        	 resultList.add(map);
	        	 }
	         }

	         rs.close();
	         ps.close();
	         LOGGER.info("---------------ProcessImageDaoImpl Ends getAllImageResultsForDuplicateImages----------------\n");
		 } catch (SQLException e){
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
		 return resultList;
	}

	@Override
	public List<Map<String, String>> getImageResultsForPremium(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageResultsForPremium :: projectId={}",projectId);

		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();

		String sql = "select imageStore.imageUUID, imageStore.taskId, imageStore.imageResultCode, imageResultMaster.description as imageResultDesc, imageResultComments,"
				+ " DATE_FORMAT(imageStore.processedDate,'%Y-%m-%dT%H:%i:%s.000Z') as processedDate, fileId"
				+ " from ImageStoreNew imageStore, ImageResultMaster imageResultMaster"
				+ " where projectId = ? and imageStore.imageStatus = 'done' and imageStore.imageReviewStatus > 0 and imageStore.resultUploaded = '0'"
				+ " and imageStore.imageResultCode is not null and imageStore.imageResultCode not in ('6','100') and imageStore.imageResultCode = imageResultMaster.resultCode";

		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, String> result = new LinkedHashMap<String,String>();
            	result.put("imageUUID", rs.getString("imageUUID"));
            	result.put("taskId", rs.getString("taskId"));
            	result.put("imageResultCode", rs.getString("imageResultCode"));
            	result.put("imageResultDesc", rs.getString("imageResultDesc"));
            	result.put("imageResultComments", ifNullToEmpty(rs.getString("imageResultComments")));
            	result.put("processedDate", rs.getString("processedDate"));
            	result.put("fileId", rs.getString("fileId"));
            	resultList.add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageResultsForPremium numberOfImages = {}",resultList.size());
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
		return resultList;
	}

	@Override
	public void updateImageResultCodeByImageHashScoreBatch(Map<String,String> imageHashScoresWithDuplicates, int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeByImageHashScoreBatch"
			+ "::projectId::{} ::imageHashScores={}", projectId,imageHashScoresWithDuplicates);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageResultCode = ?, oldComments = imageResultComments, imageResultComments = ?, imageStatus='done', resultUploaded='0', lastUpdatedTimestamp = ?, imageReviewStatus = 1 , processedDate = NOW() WHERE imageHashScore = ? and projectId = ? and imageResultCode is not null";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);

            for(Entry<String, String> entry : imageHashScoresWithDuplicates.entrySet() ) {
            	String resultCode, resultComment;
            	String value = entry.getValue();
            	String [] splitValues = value.split("#");
            	resultCode = splitValues[0];
            	if ( splitValues.length == 2 ){
            		resultComment = splitValues[1];
            	} else {
            		resultComment = "";
            	}
            	
            	ps.setString(1, resultCode);
            	ps.setString(2, resultComment);
            	ps.setString(3, String.valueOf(currTimestamp));
                ps.setString(4, entry.getKey());
                ps.setInt(5, projectId);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeByImageHashScoreBatch----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
	}

	@Override
	public void updateImageHashScoreResults(ImageStore imageStore) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageHashScoreResults::imageHashScore={}::imageResultCode::{}::imageResultComments::{}",imageStore.getImageHashScore(),imageStore.getImageResultCode(),imageStore.getImageResultComments());
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageHashScore = ?, imageResultCode = ?, imageResultComments = ?, processedDate = NOW(),"
        		+ " lastUpdatedTimestamp = ?, pixelsPerInch = ?, oosCount = ?, oosPercentage = ?, imageAngle = ?, shelfLevels = ?,"
        		+ " imageReviewRecommendations = ?, imageNotUsable = ?, imageNotUsableComment = ?, lowConfidence = ?, "
        		+ " origWidth = ?, origHeight = ?, newWidth = ?, newHeight = ? WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageStore.getImageHashScore());
            ps.setString(2, imageStore.getImageResultCode());
            ps.setString(3, imageStore.getImageResultComments());
            ps.setString(4, String.valueOf(currTimestamp));
            ps.setString(5, imageStore.getPixelsPerInch());
            ps.setString(6, imageStore.getOosCount());
            ps.setString(7, imageStore.getOosPercentage());
            ps.setString(8, imageStore.getImageAngle());
            ps.setString(9, imageStore.getShelfLevels());
            ps.setString(10, imageStore.getImageReviewRecommendations());
            ps.setString(11, imageStore.getImageNotUsable());
            ps.setString(12, imageStore.getImageNotUsableComment());
            ps.setString(13, imageStore.getLowConfidence());
            ps.setString(14, imageStore.getOrigWidth());
            ps.setString(15, imageStore.getOrigHeight());
            ps.setString(16, imageStore.getNewWidth());
            ps.setString(17, imageStore.getNewHeight());

            ps.setString(18, imageStore.getImageUUID());

            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageHashScoreResults----------------\n");

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

	@Override
	public List<String> getImagesByStoreVisit(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getImagesByStoreVisit::projectId={}storeId={}::taskId={}",projectId,storeId,taskId);
		
        String sql = "SELECT imageUUID FROM ImageStoreNew WHERE projectId = ? and storeId = ? and taskId = ?";
        List<String> imageList=new ArrayList<String>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                imageList.add(rs.getString("imageUUID"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImagesByStoreVisit ::images = "+imageList+"----------------\n");

            return imageList;
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
	public void updateImageResultCodeAndStatusBatch(List<ImageStore> imageStoreList) throws Exception {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeAndStatusBatch::listOfImages={}",imageStoreList);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageResultCode = ?, imageReviewStatus = ?, oldComments = imageResultComments, imageResultComments = ?, lastUpdatedTimestamp = ?, resultUploaded = ?, processedDate = NOW() WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            
            for(ImageStore imageStore : imageStoreList ) {
            		ps.setString(1, imageStore.getImageResultCode());
            		ps.setString(2, imageStore.getImageReviewStatus());
            		ps.setString(3, imageStore.getImageResultComments());
            		ps.setString(4, String.valueOf(currTimestamp));
            		ps.setString(5, imageStore.getResultUploaded());
            		ps.setString(6, imageStore.getImageUUID());
            		ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeAndStatusBatch----------------\n");
        } catch (Exception e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
		
	}

	@Override
	public void updateProjectStoreResultByBatchId(int projectId, String batchId, String resultCode) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateProjectStoreResultByBatchId::projectId={}, batchId={}, resultCode={}",projectId,batchId,resultCode);
        String sql = "UPDATE ProjectStoreResult SET resultCode = ? WHERE projectId = ? AND batchId = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
           	ps.setString(1, resultCode);
           	ps.setInt(2, projectId);
            ps.setString(3, batchId);
            ps.executeUpdate();
            ps.close();
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
        LOGGER.info("---------------ProcessImageDaoImpl Ends updateProjectStoreResultByBatchId----------------\n");
	}

	@Override
	public void updateBatchIdForProjectStoreResults(int projectId, List<StoreVisit> storeVisits,String batchId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateBatchIdForProjectStoreResults::projectId={}, batchId={}, storeVisits={}",projectId,batchId,storeVisits);
        String sql = "UPDATE ProjectStoreResult SET batchId = ? WHERE projectId = ? AND storeId = ? AND taskId = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            
            for(StoreVisit storeVisit : storeVisits ) {
            	ps.setString(1, batchId);
            	ps.setInt(2, projectId);
                ps.setString(3, storeVisit.getStoreId());
                ps.setString(4, storeVisit.getTaskId());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
        LOGGER.info("---------------ProcessImageDaoImpl Ends updateBatchIdForProjectStoreResults----------------\n");		
	}

    @Override
    public List<LinkedHashMap<String, String>> getAggsReadyStoreVisit() {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getAggsReadyStoreVisit----------------\n");
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        String sql = "SELECT" + 
        		"    C.projectId," + 
        		"    C.storeId," + 
        		"    C.taskId, " + 
        		"    (C.totalImagesCount - C.processedImagesCount) AS pendingImagesCount" + 
        		" FROM (" + 
        		"    SELECT" + 
        		"        A.projectId," + 
        		"        A.storeId," + 
        		"        A.taskId," + 
        		"        COUNT(*) AS totalImagesCount," + 
        		"        COUNT(IF(A.imageStatus='done',1,NULL)) processedImagesCount" + 
        		"    FROM " + 
        		"        ImageStoreNew A " + 
        		"    INNER JOIN (" + 
        		"        SELECT" + 
        		"            projectId," + 
        		"            storeId," + 
        		"            taskId" + 
        		"        FROM" + 
        		"            ProjectStoreResult" + 
        		"        WHERE" + 
        		"            resultCode='99'" + 
        		"    ) B" + 
        		"    ON A.projectId=B.projectId and A.storeId=B.storeId and A.taskId=B.taskId" + 
        		"    GROUP BY A.projectId,A.storeId,A.taskId" + 
        		" ) C" + 
        		" HAVING pendingImagesCount = 0";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectId",""+rs.getInt("projectId"));
                map.put("storeId",rs.getString("storeId"));
                map.put("taskId",rs.getString("taskId"));
                resultList.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------Number of store visits to aggregate = {}", resultList.size());

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
        LOGGER.info("---------------ProcessImageDaoImpl Ends getAggsReadyStoreVisit----------------\n");
        return resultList;
    }
    
    @Override
    public List<LinkedHashMap<String, String>> getStoreVisitsForStoreAnalysisProcessing() {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreVisitsForStoreAnalysisProcessing----------------\n");
        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        String sql = "SELECT projectId,storeId,taskId FROM ProjectStoreResult WHERE resultCode='998' AND status='0' ORDER BY processedDate ASC";
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectId",""+rs.getInt("projectId"));
                map.put("storeId",rs.getString("storeId"));
                map.put("taskId",rs.getString("taskId"));
                resultList.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------Number of store visits to process = {}", resultList.size());

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
        LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreVisitsForStoreAnalysisProcessing----------------\n");
        return resultList;
    }

	@Override
	public void changeProjectImageStatus(int projectId, String currentImageStatus, String newImageStatus) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts changeProjectImageStatus::projectId={}, currentImageStatus={}, newImageStatus={}",projectId,currentImageStatus,newImageStatus);
        String sql = "UPDATE ImageStoreNew SET imageStatus = ? WHERE projectId = ? AND imageStatus = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            
            PreparedStatement ps = conn.prepareStatement(sql);
           	ps.setString(1, newImageStatus);
           	ps.setInt(2, projectId);
            ps.setString(3, currentImageStatus);
            
            ps.executeUpdate();
            ps.close();
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
        LOGGER.info("---------------ProcessImageDaoImpl Ends changeProjectImageStatus----------------\n");
	}

	@Override
	public Map<String, Map<String, String>> getImagesForSurveyUpload(int projectId) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts getImagesForSurveyUpload :: projectId={} ", projectId );
		
		Map<String,Map<String, String>> resultMap = new LinkedHashMap<String,Map<String,String>>();
		
		String sql = "select image.imageUUID, image.imageURL, image.imageRotation, image.taskId, DATE_FORMAT(image.processedDate,'%m/%d/%Y') as processedDate, store.RetailerStoreID"
				+ " from ImageStoreNew image, StoreMaster store"
				+ " where projectId= ? and imageStatus = 'done' and image.storeId = store.StoreId";
		
		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, String> result = new LinkedHashMap<String,String>();
            	result.put("imageUUID", rs.getString("imageUUID"));
            	result.put("imageURL", rs.getString("imageURL"));
            	result.put("imageRotation", rs.getString("imageRotation"));
            	result.put("processedDate", rs.getString("processedDate"));
            	result.put("taskId", rs.getString("taskId"));
            	result.put("retailerStoreID", rs.getString("RetailerStoreID"));
            	resultMap.put(rs.getString("imageUUID"), result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImagesForSurveyUpload numberOfImages = {}",resultMap.size());
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
		return resultMap;
	}

	@Override
	public Map<String, List<Map<String, String>>> getImageAnalysisForSurveyUpload(int projectId) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageAnalysisForSurveyUpload :: projectId={}", projectId);
		
		Map<String,List<Map<String, String>>> resultMap = new LinkedHashMap<String,List<Map<String,String>>>();
		
		String sql = "select imageUUID, upc, leftTopX, leftTopY, width, height"
				+ " from ImageAnalysisNew"
				+ " where projectId = ?"
				+ " and upc in ( select upc from ProjectUpc where projectId = ? and skuTypeId in ('1','2') )";
		
		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, String> result = new LinkedHashMap<String,String>();
            	String imageUUID = rs.getString("imageUUID");
            	result.put("imageUUID", imageUUID);
            	result.put("upc", rs.getString("upc"));
            	result.put("leftTopX", rs.getString("leftTopX"));
            	result.put("leftTopY", rs.getString("leftTopY"));
            	result.put("width", rs.getString("width"));
            	result.put("height", rs.getString("height"));
            	
            	if ( resultMap.get(imageUUID) == null ) {
            		resultMap.put(imageUUID, new ArrayList<Map<String,String>>());
            	}
            	resultMap.get(imageUUID).add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageAnalysisForSurveyUpload numberOfImages = {}",resultMap.size());
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
		return resultMap;
	}

	@Override
	public Map<String, List<Map<String, String>>> getProjectStoreImageMetaData(int projectId, String storeId) {
		
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreImageMetaData----------------\n");
		String sql = "select imageUUID, imageStatus, dateId, agentId, taskId, origWidth, origHeight, newWidth, newHeight, imageRotation from ImageStoreNew "
				+ "where projectId=? and storeId=?";
		
		Map<String,List<Map<String,String>>> result=new LinkedHashMap<String,List<Map<String,String>>>();

		Connection conn = null;

        try {
            conn = dataSource.getConnection();
            
            	PreparedStatement ps = conn.prepareStatement(sql);
            	ps.setInt(1, projectId);
            	ps.setString(2, storeId);
            	
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                	String taskId = rs.getString("taskId");
                	if ( result.get(taskId) == null ) {
                		result.put(taskId, new ArrayList<Map<String,String>>());
                	}
                    Map<String, String> map = new LinkedHashMap<String, String>();
                    map.put("imageUUID", rs.getString("imageUUID"));
                    map.put("projectId", projectId+"");
                    map.put("storeId", storeId);
                    map.put("taskId", taskId);
                    map.put("imageStatus", ifNullToEmpty(rs.getString("imageStatus")));
                    map.put("dateId", rs.getString("dateId"));
                    map.put("agentId", ifNullToEmpty(rs.getString("agentId")));
                    map.put("origWidth", ifNullToEmpty(rs.getString("origWidth")));
                    map.put("origHeight", ifNullToEmpty(rs.getString("origHeight")));
                    map.put("newWidth", ifNullToEmpty(rs.getString("newWidth")));
                    map.put("newHeight", ifNullToEmpty(rs.getString("newHeight")));
                    map.put("imageRotation", ifNullToEmpty(rs.getString("imageRotation")));
                    
                    result.get(taskId).add(map);
                }
                rs.close();
                ps.close();
                
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreImageMetaData----------------\n");
            return result;
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
	public void updateProjectImageResultUploadStatus(List<String> imageUUIDs, String resultUploaded) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateProjectImageResultUploadStatus::listOfImages={}",imageUUIDs);
        String sql = "UPDATE ImageStoreNew SET resultUploaded = ?, imageStatus = ? WHERE imageUUID = ? ";
        Connection conn = null;
        
        String imageStatus = "uploaded_"+ LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            
            for(String imageUUID : imageUUIDs ) {
            	ps.setString(1, resultUploaded);
            	ps.setString(2, imageStatus);
            	ps.setString(3, imageUUID);
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateProjectImageResultUploadStatus----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
		
	}


    @Override
    public String getDistinctStoreIdCountByMonth(int projectId, List<String> childProjectIds, String month, String waveId, 
    		String subCategory, String modular, String storeFormat, String retailer) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getDistinctStoreIdCount::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "subCategory= {}, modular= {}, storeFormat= {}, retailer= {}",projectId,month,waveId,childProjectIds,subCategory,modular,storeFormat,retailer);
        String storeCount = "0";
        String sqlFileName = "getProjectBrandShares_totalStoreCount.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategory.equals("all") ? "%" : subCategory;
    	String modularFilter = modular.equals("all") ? "%" : modular;
    	String storeFormatFilter = storeFormat.equals("all") ? "%" : storeFormat;
    	String retailerFilter = retailer.equals("all") ? "%" : retailer;
        
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, modularFilter);
            ps.setString(4, month+"%");
            ps.setString(5, waveId);
            ps.setString(6, modularFilter);
            ps.setString(7, storeFormatFilter);
            ps.setString(8, retailerFilter);
            ps.setString(9, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                storeCount = rs.getString("storeCount");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getDistinctStoreIdCountByMonth :: {}----------------\n",storeCount);
            return storeCount;
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
    public String getTotalUpcs(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getTotalUpcs::projectId={}", projectId);
        String sql = "\n" +
                "\n" +
                "select count(distinct upc) as totalUpcs from \n" +
                "  (select upc,storeId,taskId from ProjectStoreData where projectId = \""+projectId+"\") a\n" +
                "join\n" +
                "  (\n" +
                "    select p1.taskId, p1.storeId from \n" +
                "      (SELECT taskId, storeId, visitDateId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ) p1  \n" +
                "    JOIN \n" +
                "      ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "    on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "  ) b\n" +
                " on a.storeId=b.storeId and a.taskId=b.taskId";

        Connection conn = null;
        try {
            String totalUpcs = null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalUpcs = rs.getString("totalUpcs");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getTotalUpcs totalUpcs = {}", totalUpcs);

            return totalUpcs;
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
    public String getTotalBrands(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getTotalBrands::projectId= {}", projectId );
        String sql = "\n" +
                "select count(distinct x.BRAND_NAME) as totalBrands  from \n" +
                "  (select UPC, BRAND_NAME from ProductMaster) x\n" +
                "join\n" +
                "  (\n" +
                "  select distinct upc from \n" +
                "    (select upc, storeId, taskId from ProjectStoreData where projectId = \""+projectId+"\") a\n" +
                "  join\n" +
                "    (\n" +
                "      select p1.taskId, p1.storeId from \n" +
                "        (SELECT taskId, storeId, visitDateId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ) p1  \n" +
                "      JOIN \n" +
                "        ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "      on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "    ) b\n" +
                "   on a.storeId=b.storeId and a.taskId=b.taskId\n" +
                "   ) y\n" +
                "on x.UPC=y.upc";

        Connection conn = null;
        try {
            String totalBrands = null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalBrands = rs.getString("totalBrands");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getTotalBrands totalBrands = {}", totalBrands);

            return totalBrands;
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
    public String getTotalFacings(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getTotalFacings::projectId={}", projectId);
        String sql = "select sum(p1.sumFacing) as totalFacings from \n" +
                "  (SELECT taskId, storeId, visitDateId, sumFacing FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ) p1  \n" +
                "JOIN \n" +
                "  ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId";

        Connection conn = null;
        try {
            String totalFacings = null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                totalFacings = rs.getString("totalFacings");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getTotalFacings totalFacings = {}", totalFacings);

            return totalFacings;
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
    public List<String> getListBrands(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListBrands::projectId={}", projectId);
        String sql = "select distinct x.BRAND_NAME   from \n" +
                "  (select UPC, BRAND_NAME from ProductMaster) x\n" +
                "join\n" +
                "  (\n" +
                "  select distinct upc from \n" +
                "    (select upc, storeId, taskId from ProjectStoreData where projectId = \""+projectId+"\") a\n" +
                "  join\n" +
                "    (\n" +
                "      select p1.taskId, p1.storeId from \n" +
                "        (SELECT taskId, storeId, visitDateId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ) p1  \n" +
                "      JOIN \n" +
                "        ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "      on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "    ) b\n" +
                "   on a.storeId=b.storeId and a.taskId=b.taskId\n" +
                "   ) y\n" +
                "on x.UPC=y.upc";

        Connection conn = null;
        try {
            List<String> brandList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                brandList.add(rs.getString("BRAND_NAME"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListBrands brandList.size = {}", brandList.size());

            return brandList;
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
    public List<String> getListBrandsNew(String customerCode, int parentProjectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListBrandsNew::customer code={},parent projectId={}", customerCode, parentProjectId);
        String sql = "SELECT" + 
        		"    DISTINCT(pm.BRAND_NAME)" + 
        		" FROM ProjectUpc pu, ProductMaster pm " + 
        		" WHERE pu.projectId IN (" + 
        		"        SELECT project.id FROM Project project " + 
        		"        INNER JOIN (" + 
        		"        	SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" + 
        		"        ) customerMappedProjects " + 
        		"        ON ( project.id = customerMappedProjects.projectId )" + 
        		"        WHERE project.parentProjectId = ? AND project.status = '1'" + 
        		"    ) AND pu.skuTypeId in ('1','2','3') AND pu.upc = pm.upc" +
        		" ORDER BY BRAND_NAME";

        Connection conn = null;
        try {
            List<String> brandList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, parentProjectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                brandList.add(rs.getString("BRAND_NAME"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListBrandsNew brandList = {}", brandList);

            return brandList;
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
    public List<String> getListManufacturers(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListManufacturers::projectId={}", projectId);
        String sql = "select distinct x.MFG_NAME   from \n" +
                "  (select UPC, MFG_NAME from ProductMaster) x\n" +
                "join\n" +
                "  (\n" +
                "  select distinct upc from \n" +
                "    (select upc, storeId, taskId from ProjectStoreData where projectId = \""+projectId+"\") a\n" +
                "  join\n" +
                "    (\n" +
                "      select p1.taskId, p1.storeId from \n" +
                "        (SELECT taskId, storeId, visitDateId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ) p1  \n" +
                "      JOIN \n" +
                "        ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "      on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "    ) b\n" +
                "   on a.storeId=b.storeId and a.taskId=b.taskId\n" +
                "   ) y\n" +
                "on x.UPC=y.upc";

        Connection conn = null;
        try {
            List<String> manufacturerList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                manufacturerList.add(rs.getString("MFG_NAME"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListManufacturers manufacturerList.size = {}",manufacturerList.size());

            return manufacturerList;
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
    public List<String> getListManufacturersNew(String customerCode, int parentProjectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListManufacturers::customer code={}, projectId={}", customerCode, parentProjectId);
        String sql = "SELECT" + 
        		"    DISTINCT(pm.MFG_NAME)" + 
        		" FROM ProjectUpc pu, ProductMaster pm " + 
        		" WHERE pu.projectId IN (" + 
        		"        SELECT project.id FROM Project project " + 
        		"        INNER JOIN (" + 
        		"        	SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" + 
        		"        ) customerMappedProjects " + 
        		"        ON ( project.id = customerMappedProjects.projectId )" + 
        		"        WHERE project.parentProjectId = ? AND project.status = '1'" + 
        		"    ) AND pu.skuTypeId in ('1','2','3') AND pu.upc = pm.upc" +
        		" ORDER BY MFG_NAME";
        
        Connection conn = null;
        try {
            List<String> manufacturerList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, parentProjectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                manufacturerList.add(rs.getString("MFG_NAME"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListManufacturersNew manufacturerList = {}",manufacturerList);

            return manufacturerList;
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
    public String getAverageUpcsPerStore(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getAverageUpcsPerStore::projectId={}", projectId);
        String sql = "  select avg(count) as avgUpcCount from (\n" +
                "      select p1.taskId, p1.storeId, p1.countDistinctUpc  as count from \n" +
                "        (SELECT taskId, storeId, visitDateId, countDistinctUpc FROM ProjectStoreResult WHERE projectId = \""+ projectId +"\" AND STATUS = 1 ) p1  \n" +
                "      JOIN \n" +
                "        ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "      on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "    ) b";

        Connection conn = null;
        try {
            String averageUpcsPerStore =null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                averageUpcsPerStore = rs.getString("avgUpcCount");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getAverageUpcsPerStore averageUpcsPerStore={}", averageUpcsPerStore);

            return averageUpcsPerStore;
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
    public String getAverageFacingPerStore(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getAverageFacingPerStore::projectId={}", projectId);
        String sql = "select avg(sumFacing) as avgSumFacing from (\n" +
                "      select p1.taskId, p1.storeId, p1.sumFacing  as sumFacing  from \n" +
                "        (SELECT taskId, storeId, visitDateId, sumFacing FROM ProjectStoreResult WHERE projectId = \""+ projectId +"\" AND STATUS = 1 ) p1  \n" +
                "      JOIN \n" +
                "        ( SELECT max(visitDateId) AS visitDateId, storeId FROM ProjectStoreResult WHERE projectId = \""+ projectId +"\" AND STATUS = 1 GROUP BY storeId ) p2 \n" +
                "      on p1.storeId = p2.storeId AND p1.visitDateId = p2.visitDateId\n" +
                "    ) b";

        Connection conn = null;
        try {
            String averageFacingPerStore =null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                averageFacingPerStore = rs.getString("avgSumFacing");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getAverageFacingPerStore averageFacingPerStore= {}", averageFacingPerStore);

            return averageFacingPerStore;
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
    public List<String> getListMonths(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListMonths::projectId={}", projectId);
        String sql = " SELECT distinct SUBSTRING(visitDateId, 1, 6) as months  FROM ProjectStoreResult WHERE projectId = \""+projectId+"\" AND STATUS = 1 ";

        Connection conn = null;
        try {
            List<String> monthList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		String oneMonth = rs.getString("months");
            		if (StringUtils.isNotBlank(oneMonth)) {
            			monthList.add(oneMonth);
            		}
            	}
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListMonths monthList.size = {}", monthList.size());

            return monthList;
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
    public List<String> getListMonthsNew(String customerCode, int parentProjectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getListMonthsNew::parent projectId={}, customerCode={}", parentProjectId,customerCode);
        String sql = " SELECT DISTINCT SUBSTRING(visitDateId, 1, 6) AS months  FROM ProjectStoreResult "
        		+ " WHERE projectId IN ("
        		+ "      SELECT project.id FROM Project project " 
        		+ "      INNER JOIN (" 
        		+ "            SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?"
        		+ "      ) customerMappedProjects "
        		+ "      ON ( project.id = customerMappedProjects.projectId )" 
        		+ "      WHERE project.parentProjectId = ? AND project.status = '1'" 
        		+ "     )"
        		+ "AND status = '1' "
        		+ "ORDER BY months";

        Connection conn = null;
        try {
            List<String> monthList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, parentProjectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		String oneMonth = rs.getString("months");
            		if (StringUtils.isNotBlank(oneMonth)) {
            			monthList.add(oneMonth);
            		}
            	}
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListMonthsNew monthList.size = {}", monthList.size());

            return monthList;
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
    public String isDistributionCheckProject(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts isDistributionCheckProject::projectId={}", projectId);
        String sql =  "select projectTypeId from Project where id = \""+projectId+"\"" ;

        Connection conn = null;
        try {
            String  projectTypeId=null;
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                projectTypeId=rs.getString("projectTypeId");
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends isDistributionCheckProject projectTypeId = {}", projectTypeId);

            return projectTypeId;
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
    public void insertIntoProjectDistributionStoreData(int projectId, String storeId, String taskId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts insertIntoProjectDistributionStoreData::projectId={}, storeId={}, taskId={}", projectId, storeId, taskId);

        String deleteSql ="delete from ProjectDistributionStoreData where projectId=? and storeId=? and taskId=?";



        String insertSql =  "INSERT INTO ProjectDistributionStoreData\n" +
                " (projectId,storeId,taskId,storeType,upc,facing,price,promotion,imageUUID,isMissing,compliant) \n" +
                "SELECT CASE WHEN b.projectId IS NULL THEN \""+projectId+"\" ELSE b.projectId END AS projectId\n" +
                "    ,CASE WHEN b.storeId IS NULL THEN \""+ storeId + "\" ELSE b.storeId END AS storeId\n" +
                "    ,CASE WHEN b.taskId IS NULL THEN \""+taskId+"\" ELSE b.taskId END AS taskId\n" +
                "    ,a.storeType AS storeType\n" +
                "    ,a.upc AS upc\n" +
                "    ,CASE WHEN b.facing IS NULL THEN \"0\" ELSE b.facing END AS facing\n" +
                "    ,CASE WHEN b.price IS NULL THEN \"0.0\" ELSE b.price END AS price\n" +
                "    ,CASE WHEN b.promotion IS NULL THEN \"0\" ELSE b.promotion END AS promotion\n" +
                "    ,b.imageUUID AS imageUUID\n" +
                "    ,CASE WHEN b.upc IS NULL THEN true ELSE false END AS isMissing\n" +
                "    ,CASE WHEN b.compliant IS NULL THEN \"0\" ELSE b.compliant END AS compliant\n" +
                "FROM (\n" +
                "    SELECT upc\n" +
                "        ,storeType\n" +
                "    FROM ProjectDistributionUpc\n" +
                "    WHERE storeType IN (\n" +
                "            SELECT storeType\n" +
                "            FROM ProjectDistributionStoreType\n" +
                "            WHERE projectId = \""+projectId+"\" \n" +
                "                AND storeId =  \""+storeId+"\" \n" +
                "            )\n" +
                "        AND projectId = \""+projectId+"\" \n" +
                "    ) a\n" +
                "LEFT JOIN (\n" +
                "    SELECT *\n" +
                "    FROM ProjectStoreData\n" +
                "    WHERE projectId = \""+projectId+"\" \n" +
                "        AND storeId = \""+storeId+"\" \n" +
                "        AND taskId =\""+taskId+"\" \n" +
                "    ) b ON a.upc = b.upc " ;

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps1 = conn.prepareStatement(deleteSql);

            ps1.setInt(1,projectId);
            ps1.setString(2,storeId);
            ps1.setString(3,taskId);

            PreparedStatement ps2 = conn.prepareStatement(insertSql);

            LOGGER.info("---------------ProcessImageDaoImpl Ends insertIntoProjectDistributionStoreData deleteSql={}",deleteSql);
            LOGGER.info("---------------ProcessImageDaoImpl Ends insertIntoProjectDistributionStoreData insertSql={}",insertSql);

            ps1.execute();
            ps2.executeUpdate();

            ps1.close();
            ps2.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends insertIntoProjectDistributionStoreData ----------------\n");
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
    public void updateProjectStoreResult(int projectId, String storeId, String taskId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateProjectStoreResult::projectId={}, storeId={}, taskId={}", projectId, storeId,taskId);
        String sql =  "SELECT *\n" +
                "    ,countDistributionUpc / (countDistributionUpcMissing + countDistributionUpc) AS distribution\n" +
                "FROM (\n" +
                "    SELECT sum(facing) AS facing\n" +
                "        ,sum(CASE \n" +
                "                WHEN isMissing = 1\n" +
                "                    THEN count\n" +
                "                ELSE \"0\"\n" +
                "                END) AS countDistributionUpcMissing\n" +
                "        ,sum(CASE \n" +
                "                WHEN isMissing = 0\n" +
                "                    THEN count\n" +
                "                ELSE \"0\"\n" +
                "                END) AS countDistributionUpc\n" +
                "    FROM (\n" +
                "        SELECT isMissing\n" +
                "            ,count(DISTINCT upc) AS count\n" +
                "            ,sum(facing) AS facing\n" +
                "        FROM ProjectDistributionStoreData\n" +
                "        WHERE storeId =  \""+storeId+"\" \n" +
                "            AND taskId =  \""+taskId+"\" \n" +
                "            AND projectId = \""+projectId+"\"\n" +
                "        GROUP BY isMissing\n" +
                "        ) a\n" +
                "    )\n" +
                "\n" +
                "b;" ;

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            String facing="0";
            String distribution="0";
            String countDistributionUpcMissing="0";
            String countDistributionUpc="0";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                facing=rs.getString("facing");
                distribution=rs.getString("distribution");
                countDistributionUpcMissing=rs.getString("countDistributionUpcMissing");
                countDistributionUpc=rs.getString("countDistributionUpc");
            }
            rs.close();
            ps.close();

            String sql2 = "UPDATE ProjectStoreResult\n" +
                    "SET countDistributionUpc = \""+countDistributionUpc+"\"\n" +
                    "    ,countDistributionUpcMissing = \""+countDistributionUpcMissing+"\"\n" +
                    "    ,sumDistributionFacing = \""+facing+"\"\n" +
                    "    ,distribution = \""+distribution+"\"\n" +
                    "WHERE storeId =\""+storeId+"\" \n" +
                    "    AND taskId = \""+taskId+"\" \n" +
                    "    AND projectId =  \""+projectId+"\"\n" ;

            PreparedStatement ps2 = conn.prepareStatement(sql2);
            ps2.executeUpdate();
            ps2.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends updateProjectStoreResult facing = {}" + facing + "distribution = {}, countDistributionUpcMissing = {}, countDistributionUpc = {}", facing,distribution,countDistributionUpcMissing, countDistributionUpc);
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
	public Map<String, List<Map<String, String>>> getProjectStoreDistributionData(int projectId, String storeId) {

		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreDistributionData::projectId={}, storeId={}",projectId, storeId);

		String distinctTaskIdsSql = "select distinct(taskId) as taskId from ProjectDistributionStoreData where projectId= \""
				+ projectId + "\" and storeId = \"" + storeId
				+ "\"";

		String sql = "select a.imageUUID,a.projectId,a.storeId, a.taskId, a.upc,  a.facing,a.price, a.promotion, b.BRAND_NAME,b.PRODUCT_SHORT_NAME,b.PRODUCT_LONG_NAME, a.shelfLevel from\n"
				+ "ProjectDistributionStoreData a, ProductMaster b where a.projectId=? and a.storeId=? and a.taskId = ? and a.upc=b.upc";

		Connection conn = null;
		List<String> taskIdList = new ArrayList<String>();

		Map<String, List<Map<String, String>>> result = new LinkedHashMap<String, List<Map<String, String>>>();

		try {
			conn = dataSource.getConnection();

			PreparedStatement distinctTaskPs = conn.prepareStatement(distinctTaskIdsSql);
			ResultSet distinctTaskRs = distinctTaskPs.executeQuery();
			while (distinctTaskRs.next()) {
				taskIdList.add(distinctTaskRs.getString("taskId"));
			}
			distinctTaskRs.close();
			distinctTaskPs.close();

			for (String taskId : taskIdList) {
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();

				PreparedStatement ps = conn.prepareStatement(sql);
				ps.setInt(1, projectId);
				ps.setString(2, storeId);
				ps.setString(3, taskId);

				ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					Map<String, String> map = new LinkedHashMap<String, String>();
					map.put("imageUUID", rs.getString("imageUUID"));
					map.put("projectId", rs.getInt("projectId")+"");
					map.put("storeId", storeId);
					map.put("taskId", taskId);
					map.put("upc", rs.getString("upc"));
					map.put("brand_name", rs.getString("BRAND_NAME"));
					map.put("product_short_name", rs.getString("PRODUCT_SHORT_NAME"));
					map.put("product_long_name", rs.getString("PRODUCT_LONG_NAME"));
					map.put("facing", rs.getString("facing"));
					map.put("price", rs.getString("price"));
					map.put("promotion", rs.getString("promotion"));
					map.put("shelfLevel", ConverterUtil.ifNullToEmpty(rs.getString("shelfLevel")));

					list.add(map);
				}
				rs.close();
				ps.close();

				result.put(taskId, list);
			}

			LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreData----------------\n");
			return result;
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
	public List<Map<String, String>> getStoreLevelDistribution(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreLevelDistribution :: projectId={}",projectId);
		
		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		
		String sql = "SELECT \n" + 
				"	PDSD.projectId,\n" + 
				"	PDSD.storeId,\n" + 
				"	PDSD.taskId,\n" + 
				"	StoreResult.visitDateId,\n" + 
				"    StoreResult.waveId,\n" + 
				"    PWV.waveName,\n" + 
				"	SM.retailerStoreId,\n" + 
				"	SM.retailer,\n" + 
				"    SM.street,\n" + 
				"	SM.city,\n" + 
				"	SM.state,\n" + 
				"	SM.zip,\n" + 
				"	PDSD.upc,\n" + 
				"	PM.brand_name,\n" + 
				"	PM.product_short_name,\n" + 
				"	PDSD.facing\n" + 
				"FROM\n" + 
				"  (\n" + 
				"	SELECT \n" + 
				"	    projectId,\n" + 
				"		storeId,\n" + 
				"		visitDateId,\n" + 
				"        taskId,\n" + 
				"        COALESCE(waveId,'') AS waveId\n" + 
				"	FROM\n" + 
				"        ProjectStoreResult\n" + 
				"    WHERE\n" + 
				"        STATUS = 1 AND PROJECTID = ? AND (PROJECTID , STOREID, TASKID, VISITDATEID) IN (\n" + 
				"            SELECT\n" + 
				"                PROJECTID,\n" + 
				"                    STOREID,\n" + 
				"                    TASKID,\n" + 
				"                    MAX(VISITDATEID)\n" + 
				"            FROM\n" + 
				"                ProjectStoreResult\n" + 
				"            WHERE\n" + 
				"                STATUS = '1' AND PROJECTID = ? \n" + 
				"            GROUP BY PROJECTID , STOREID, TASKID )\n" + 
				"    ) StoreResult\n" + 
				"	INNER JOIN ProjectDistributionStoreData PDSD\n" + 
				"		ON ( PDSD.projectId = StoreResult.projectId AND PDSD.storeId = StoreResult.storeId AND PDSD.taskId = StoreResult.taskId)\n" + 
				"	INNER JOIN StoreMaster SM\n" + 
				"		ON ( PDSD.storeId = SM.storeId )\n" + 
				"	INNER JOIN ProductMaster PM\n" + 
				"		ON ( PDSD.upc = PM.upc )\n" + 
				"    INNER JOIN ProjectWaveConfig PWV\n" + 
				"        ON ( StoreResult.waveId = PWV.waveId AND StoreResult.projectId = PWV.projectId)\n" + 
				"ORDER BY\n" + 
				"	SM.retailerStoreId, StoreResult.waveId, PDSD.upc;";
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		Map<String, String> result = new LinkedHashMap<String,String>();
            		result.put("storeId", rs.getString("storeId"));
            		result.put("taskId", rs.getString("taskId"));
            		result.put("retailerStoreId", rs.getString("retailerStoreId"));
            		result.put("retailer", rs.getString("retailer"));
            		result.put("street", rs.getString("street"));
            		result.put("city", rs.getString("city"));
            		result.put("state", rs.getString("state"));
            		result.put("zip", rs.getString("zip"));
            		String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
            		if ( !visitDate.isEmpty() ) {
            			try {
            				visitDate = outSdf.format(inSdf.parse(visitDate));
            			} catch (ParseException e) {
            				LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            			}
            		}
            		result.put("visitDate", visitDate);
            		result.put("upc", rs.getString("upc"));
            		result.put("brand_name", rs.getString("brand_name"));
            		result.put("product_short_name", rs.getString("product_short_name"));
            		result.put("facing", rs.getString("facing"));
            		result.put("waveId", rs.getString("waveId"));
            		result.put("waveName", rs.getString("waveName"));
            		resultList.add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreLevelDistribution----------------\n");
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
		return resultList;
	}

	@Override
	public List<Map<String, String>> getDetailedBrandShareByMonth(int projectId, String month, String rollup) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getDetailedBrandShareByMonth::projectId= {}, rollup= {}, month= {}",projectId,rollup,month);
        String sqlFileName = "getProjectBrandShares_"+rollup+".sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        boolean isBrandRollup = "brand".equals(rollup);
        
        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, month+"%");
            ps.setInt(3, projectId);
            ps.setString(4, month+"%");
            ps.setInt(5, projectId);
            ps.setString(6, month+"%");
            ps.setInt(7, projectId);
            ps.setString(8, month+"%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("projectId", rs.getInt("projectId")+"");
                map.put("stores", rs.getString("STORES"));
                if ( isBrandRollup ) {
                	    map.put("brandName", rs.getString("BRAND_NAME"));
                    map.put("brandUpc", rs.getString("BRAND_UPC"));
                    map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                    map.put("brandUpcShare", rs.getString("BRAND_UPC_SHARE"));
                    map.put("brandFacing", rs.getString("BRAND_FACING"));
                    map.put("brandFacesShare", rs.getString("BRAND_FACES_SHARE"));
                } else {
                    map.put("mfgName", rs.getString("MFG_NAME"));
                    map.put("mfgUpc", rs.getString("MFG_UPC"));
                    map.put("mfgTotalUpc", rs.getString("MFG_TOTAL_UPC"));
                    map.put("mfgUpcShare", rs.getString("MFG_UPC_SHARE"));
                    map.put("mfgFacing", rs.getString("MFG_FACING"));
                    map.put("mfgFacesShare", rs.getString("MFG_FACES_SHARE"));
                }
                map.put("avgUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("avgFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getDetailedBrandShareByMonth----------------\n");
            return result;
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
	public List<Map<String, String>> getDetailedBrandShareByMonthNew(int projectId, List<String> childProjectIds, String month, String waveId, String rollup,
			String subCategory, String modular, String storeFormat, String retailer) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getDetailedBrandShareByMonthNew::projectId= {}, rollup= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "subCategory= {}, modular= {}, storeFormat= {}, retailer= {}",projectId,rollup,month,waveId,childProjectIds,subCategory,modular,storeFormat, retailer);
		
        String sqlFileName = "getProjectBrandShares_"+rollup+"_new.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategory.equals("all") ? "%" : subCategory;
    	String modularFilter = modular.equals("all") ? "%" : modular;
    	String storeFormatFilter = storeFormat.equals("all") ? "%" : storeFormat;
    	String retailerFilter = retailer.equals("all") ? "%" : retailer;
        
        boolean isBrandRollup = "brand".equals(rollup);
        
        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, modularFilter);
            ps.setString(4, month+"%");
            ps.setString(5, waveId);
            ps.setString(6, modularFilter);
            ps.setString(7, storeFormatFilter);
            ps.setString(8, retailerFilter);
            ps.setString(9, subCategoryFilter);
            ps.setString(10, month+"%");
            ps.setString(11, waveId);
            ps.setString(12, modularFilter);
            ps.setString(13, month+"%");
            ps.setString(14, waveId);
            ps.setString(15, modularFilter);
            ps.setString(16, storeFormatFilter);
            ps.setString(17, retailerFilter);
            ps.setString(18, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("stores", rs.getString("STORES"));
                if ( isBrandRollup ) {
                	map.put("brandName", rs.getString("BRAND_NAME"));
                    map.put("brandUpc", rs.getString("BRAND_UPC"));
                    map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                    map.put("brandUpcShare", rs.getString("BRAND_UPC_SHARE"));
                    map.put("brandFacing", rs.getString("BRAND_FACING"));
                    map.put("brandFacesShare", rs.getString("BRAND_FACES_SHARE"));
                } else {
                    map.put("mfgName", rs.getString("MFG_NAME"));
                    map.put("mfgUpc", rs.getString("MFG_UPC"));
                    map.put("mfgTotalUpc", rs.getString("MFG_TOTAL_UPC"));
                    map.put("mfgUpcShare", rs.getString("MFG_UPC_SHARE"));
                    map.put("mfgFacing", rs.getString("MFG_FACING"));
                    map.put("mfgFacesShare", rs.getString("MFG_FACES_SHARE"));
                }
                map.put("avgUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("avgFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getDetailedBrandShareByMonthNew----------------\n");
            return result;
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
	public List<Map<String, String>> getListStores(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getListStores::projectId={}", projectId);
        String sql = "SELECT a.storeId, b.state, b.city, b.street from "
        		+ "(SELECT distinct(storeId) FROM ProjectStoreResult WHERE projectId = ? AND STATUS = 1 ) a,"
        		+ " StoreMaster b"
        		+ " where a.storeId=b.storeId;";
        List<Map<String,String>> listStoresMap = new ArrayList<Map<String,String>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		Map<String,String> store = new HashMap<String,String>();
            		store.put("storeId", rs.getString("storeId"));
            		store.put("state", rs.getString("state"));
            		store.put("city", rs.getString("city"));
            		store.put("street", rs.getString("street"));
            		listStoresMap.add(store);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListStores storeList.size = {}", listStoresMap.size());

            return listStoresMap;
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
	
	public List<Map<String, String>> getShelfLevelFacings(int projectId, String month, String brand){
		
		
		String sql = "select C.BRAND_NAME as brandName, C.shelfLevel as shelfLevel , sum(C.SUM_FACINGS) as facings from\n" + 
				"(select B.UPC, B.BRAND_NAME, sum(A.SUM_FACINGS) as SUM_FACINGS, A.storeId , A.shelfLevel , A.taskId  from\n" + 
				"(select distinct(upc) as upc, sum(facing) AS SUM_FACINGS, storeId, shelfLevel, taskId from ProjectStoreData where projectId =? \n" +
				"and taskId IN (SELECT taskId FROM ProjectStoreResult WHERE projectId=? AND visitDateId like ?) group by storeId, upc, shelfLevel) A \n" +
				"INNER JOIN (select UPC, BRAND_NAME from ProductMaster where UPC <> '999999999999' ) B \n" + 
				"ON A.upc = B.UPC group by  B.BRAND_NAME, B.UPC, A.storeId, A.shelfLevel) C group by C.BRAND_NAME,C.shelfLevel order by C.BRAND_NAME,(\n" + 
				"CASE C.shelfLevel\n" + 
				"WHEN 'Top' THEN 1\n" + 
				"WHEN 'Middle' THEN 2\n" + 
				"WHEN 'Bottom' THEN 3\n" + 
				"WHEN 'NA' THEN 4\n" + 
				"END\n" + 
				" )ASC";
		
		/*String sql = "select C.shelfLevel as shelfLevel , sum(C.SUM_FACINGS) as facings from\n" + 
				"(select B.UPC, B.BRAND_NAME, sum(A.SUM_FACINGS) as SUM_FACINGS, A.storeId , A.shelfLevel , A.taskId  from\n" + 
				"(select distinct(upc) as upc, sum(facing) AS SUM_FACINGS, storeId, shelfLevel, taskId from ProjectStoreData where customerCode=? and customerProjectId =? \n" + 
				"and taskId IN (SELECT taskId FROM ProjectStoreResult WHERE customerCode=? AND customerProjectId=? AND visitDateId like ?) group by storeId, upc, shelfLevel) A \n" + 
				"INNER JOIN (select UPC, BRAND_NAME from ProductMaster where BRAND_NAME = ? and UPC <> '999999999999') B \n" + 
				"ON A.upc = B.UPC group by  B.BRAND_NAME, B.UPC, A.storeId, A.shelfLevel) C group by C.BRAND_NAME,C.shelfLevel order by C.BRAND_NAME,(\n" + 
				"CASE C.shelfLevel\n" + 
				"WHEN 'Top' THEN 1\n" + 
				"WHEN 'Middle' THEN 2\n" + 
				"WHEN 'Bottom' THEN 3\n" + 
				"WHEN 'NA' THEN 4\n" + 
				"END\n" + 
				" )ASC";*/
		
		List<Map<String, String>> shelfFacings = new ArrayList<Map<String, String>>();
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, projectId);
			ps.setInt(2, projectId);
			ps.setString(3, month + "%");
			//ps.setString(6, brand);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, String> level = new HashMap<String, String>();
				level.put("brandName", rs.getString("brandName"));
				level.put("levelName", rs.getString("shelfLevel"));
				level.put("facingCount", rs.getString("facings"));
				shelfFacings.add(level);
			}
			rs.close();
			ps.close();
		return shelfFacings;
		
		
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
	public List<Map<String, String>> getShelfLevelFacingsNew(int projectId, List<String> childProjectIds, String month, String waveId,
			String subCategory, String modular, String storeFormat, String retailer){
		
		
		String sqlFileName = "getBrandShelfLevelFacings.sql";
		String query = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
		
		String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	query = query.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategory.equals("all") ? "%" : subCategory;
    	String modularFilter = modular.equals("all") ? "%" : modular;
    	String storeFormatFilter = storeFormat.equals("all") ? "%" : storeFormat;
    	String retailerFilter = retailer.equals("all") ? "%" : retailer;
		
		List<Map<String, String>> shelfFacings = new ArrayList<Map<String, String>>();
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
			PreparedStatement ps = conn.prepareStatement(query);
			ps.setString(1, month + "%");
			ps.setString(2, waveId);
			ps.setString(3, modularFilter);
			ps.setString(4, storeFormatFilter);
			ps.setString(5, retailerFilter);
			ps.setString(6, subCategoryFilter);

			//ps.setString(6, brand);

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Map<String, String> level = new HashMap<String, String>();
				level.put("brandName", rs.getString("brandName"));
				level.put("levelName", rs.getString("shelfLevel"));
				level.put("facingCount", rs.getString("brandLevelFacings"));
				shelfFacings.add(level);
			}
			rs.close();
			ps.close();
		return shelfFacings;
		
		
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
	public List<Map<String, String>> getProjectBrandProducts(int projectId, String month, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBrandProducts :: projectId={},month={},waveId={}",projectId,month,waveId);
		
		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		
		String sql = "SELECT\n" + 
				"           A.projectId                                          projectId,\n" +
				"           D.Retailer                                              retailer,\n" +
				"           A.storeId                                               storeId,\n" + 
				"           D.RetailerStoreID                                       retailerStoreId,\n" + 
				"           D.Street                                                street,\n" + 
				"           D.City                                                  city,\n" + 
				"           D.State                                                 state,\n" + 
				"           D.Zip                                                   zip,\n" + 
				"           D.storeFormat                                           storeFormat,\n" + 
				"           A.taskId                                                taskId,\n" + 
				"           SUBSTR(A.visitDateId,1,6)                               MONTH,\n" + 
				"           visitDateId                                             visitDateId,\n" +
				"           linearFootage                                           linearFootage,\n" +
				"           C.BRAND_NAME                                            BRAND_NAME,\n" + 
				"           B.upc                                                   UPC,\n" + 
				"           C.PRODUCT_SHORT_NAME                                    PRODUCT_SHORT_NAME,\n" + 
				"           C.PRODUCT_SUB_TYPE                                      PRODUCT_SUB_TYPE,\n" + 
				"           B.facing                                                PRODUCT_FACING,\n" + 
				"           B.price                                                 PRODUCT_PRICE,\n" + 
				"           B.promotion                                             PRODUCT_PROMOTED,\n" + 
				"           B.shelfLevel                                            PRODUCT_SHELF_LEVEL\n" + 
				"       FROM (\n" + 
				"               SELECT\n" + 
				"                  projectId,\n" +
				"                  storeId,\n" +
				"                  taskId,\n" + 
				"                  visitDateId,\n" +
				"                  linearFootage\n" +
				"               FROM\n" + 
				"                  ProjectStoreResult\n" + 
				"               WHERE\n" + 
				"                  STATUS = 1 AND  projectId = ?  and visitDateId  like ? AND COALESCE(waveId,'') LIKE ? AND\n" +
				"                  (projectId, storeId, visitDateId) IN\n" +
				"                  (\n" + 
				"                     SELECT\n" + 
				"                        projectId,\n" +
				"                        storeId,\n" +
				"                        MAX(visitDateId)\n" + 
				"                     FROM\n" + 
				"                        ProjectStoreResult\n" + 
				"                     WHERE\n" + 
				"                        STATUS = '1' AND projectId = ? and visitDateId  like ? AND COALESCE(waveId,'') LIKE ? \n" +
				"                     GROUP BY\n" + 
				"                        projectId,\n" +
				"                        storeId\n" +
				"                   )\n" + 
				"          )A,\n" + 
				"          ProjectStoreData          B,\n" + 
				"          ProductMaster             C,\n" + 
				"          StoreMaster               D\n" + 
				"       WHERE\n" + 
				"          A.projectId      = B.projectId      AND\n" +
				"          A.storeId           = B.storeId           AND\n" +
				"          A.taskId            = B.taskId            AND\n" + 
				"          B.UPC               = C.UPC               AND\n" + 
				"          B.UPC               !='999999999999'      AND\n" + 
				"          B.SKUTYPEID         IN ('1','2','3')      AND\n" + 
				"          A.storeId           = D.storeId\n" + 
				"       GROUP BY\n" + 
				"          A.projectId,\n" +
				"          A.storeId,\n" +
				"          A.taskId,\n" + 
				"          B.UPC\n" + 
				"        ORDER BY\n" + 
				"        projectId,\n" +
				"        storeId,\n" +
				"        BRAND_NAME;\n";
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, month+"%");
            ps.setString(3, waveId);
            ps.setInt(4, projectId);
            ps.setString(5, month+"%");
            ps.setString(6, waveId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            		Map<String, String> result = new LinkedHashMap<String,String>();
            		result.put("retailerStoreId", rs.getString("retailerStoreId"));
            		result.put("retailer", rs.getString("retailer"));
            		result.put("street", rs.getString("street"));
            		result.put("city", rs.getString("city"));
            		result.put("state", rs.getString("state"));
            		result.put("zip", rs.getString("zip"));
            		result.put("storeFormat", ifNullToEmpty(rs.getString("storeFormat")));
            		String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
	            if ( !visitDate.isEmpty() ) {
	               try {
	            	   	visitDate = outSdf.format(inSdf.parse(visitDate));
	               } catch (ParseException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	               }
	            }
            		result.put("visitDate", visitDate);
            		result.put("linearFootage", rs.getString("linearFootage"));
            		result.put("brand", rs.getString("BRAND_NAME"));
            		result.put("upc", rs.getString("UPC"));
            		result.put("product_sub_type", ifNullToEmpty(rs.getString("PRODUCT_SUB_TYPE")));
            		result.put("product_short_name", rs.getString("PRODUCT_SHORT_NAME"));
            		result.put("facing", rs.getString("PRODUCT_FACING"));
            		result.put("price", rs.getString("PRODUCT_PRICE"));
            		result.put("promoted", rs.getString("PRODUCT_PROMOTED"));
            		result.put("shelfLevel", rs.getString("PRODUCT_SHELF_LEVEL"));

            		resultList.add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBrandProducts----------------\n");
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
		return resultList;
	}
	
	/**
	 * Method to get ImageStoreNew by list of projects.
	 */
	@Override
    public Map<String, List<LinkedHashMap<String,String>>> getImagesByProjectIdList(List<String> projectIdList) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImagesByProjectIdList:: ids ={}", projectIdList);
        
        String sql = "SELECT imageUUID, projectId, imageStatus, imageReviewStatus,resultUploaded,imageResultCode FROM ImageStoreNew"
        		+ " WHERE projectId in ("+ projectIdList.toString().replaceAll("\\[", "").replaceAll("\\]","") +")";
        
        Map<String, List<LinkedHashMap<String,String>>> imageStoreList=new HashMap<String,List<LinkedHashMap<String,String>>>();
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String,String> map =new LinkedHashMap<String,String>();
                map.put("imageUUID", rs.getString("imageUUID"));
                map.put("imageStatus", rs.getString("imageStatus"));
                map.put("imageReviewStatus", rs.getString("imageReviewStatus"));
                map.put("resultUploaded", rs.getString("resultUploaded"));
                map.put("imageResultCode", ifNullToEmpty(rs.getString("imageResultCode")));
                
                if(imageStoreList.containsKey(rs.getString("projectId"))) {
                	imageStoreList.get(rs.getString("projectId")).add(map);
                }else{
                	List<LinkedHashMap<String, String>> projectResult = new ArrayList<LinkedHashMap<String, String>>();
                	projectResult.add(map);
                	imageStoreList.put(rs.getString("projectId"), projectResult);
                }
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImagesByProjectIdList numberOfRows = {}",imageStoreList.size());

            return imageStoreList;
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
	public Map<String, List<Map<String, String>>> getProjectStoreImageWithDetectionsMetaData(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreImageWithDetectionsMetaData----------------\n");
		String sql = "SELECT \n" + 
				"    image.imageUUID,\n" + 
				"    image.imageStatus,\n" + 
				"    image.dateId,\n" + 
				"    image.agentId,\n" + 
				"    image.taskId,\n" + 
				"    image.origWidth,\n" + 
				"    image.origHeight,\n" + 
				"    image.newWidth,\n" + 
				"    image.newHeight,\n" + 
				"    image.imageRotation,\n" + 
				"    COALESCE(image.questionId,'') AS questionId,\n" + 
				"    COALESCE(questions.questionGroupName,'') AS questionGroupName,\n" +
				"    COALESCE(image.sequenceNumber,'1') AS sequenceNumber\n" +
				"FROM \n" + 
				"    ( SELECT projectId,imageUUID,imageStatus,dateId,agentId,taskId,origWidth,origHeight,newWidth,newHeight,imageRotation,questionId,sequenceNumber\n" + 
				"         FROM ImageStoreNew WHERE projectId = ? AND storeId = ? AND taskId LIKE ? ) image\n" + 
				"    LEFT JOIN\n" + 
				"    ( SELECT projectId, questionId, questionGroupName from ProjectRepQuestions WHERE projectId = ? ) questions\n" + 
				"    ON\n" + 
				"    image.projectId = questions.projectId AND\n" + 
				"    image.questionId = questions.questionId" +
				"    ORDER BY CAST(sequenceNumber AS UNSIGNED)";

		String taskIdFilter = "%";
        if(null != taskId && !taskId.equalsIgnoreCase("-9") && !taskId.isEmpty()) {
            taskIdFilter = taskId;
        }

		Map<String,List<Map<String,String>>> result=new LinkedHashMap<String,List<Map<String,String>>>();

		Connection conn = null;

        try {
            conn = dataSource.getConnection();
            
            	PreparedStatement ps = conn.prepareStatement(sql);
            	ps.setInt(1, projectId);
            	ps.setString(2, storeId);
            	ps.setString(3, taskIdFilter);
            	ps.setInt(4, projectId);

                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                	String populatedTaskId = rs.getString("taskId");
                	if ( result.get(populatedTaskId) == null ) {
                		result.put(populatedTaskId, new ArrayList<Map<String,String>>());
                	}
                    Map<String, String> map = new LinkedHashMap<String, String>();
                    map.put("imageUUID", rs.getString("imageUUID"));
                    map.put("projectId", projectId+"");
                    map.put("storeId", storeId);
                    map.put("taskId", populatedTaskId);
                    map.put("imageStatus", ifNullToEmpty(rs.getString("imageStatus")));
                    map.put("dateId", rs.getString("dateId"));
                    map.put("agentId", ifNullToEmpty(rs.getString("agentId")));
                    map.put("origWidth", ifNullToEmpty(rs.getString("origWidth")));
                    map.put("origHeight", ifNullToEmpty(rs.getString("origHeight")));
                    map.put("newWidth", ifNullToEmpty(rs.getString("newWidth")));
                    map.put("newHeight", ifNullToEmpty(rs.getString("newHeight")));
                    map.put("imageRotation", ifNullToEmpty(rs.getString("imageRotation")));
                    map.put("questionId", ifNullToEmpty(rs.getString("questionId")));
                    map.put("questionGroupName", ifNullToEmpty(rs.getString("questionGroupName")));
                    map.put("sequenceNumber", ifNullToEmpty(rs.getString("sequenceNumber")));
                    
                    result.get(populatedTaskId).add(map);
                }
                rs.close();
                ps.close();
                
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreImageWithDetectionsMetaData----------------\n");
            return result;
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
    public List<LinkedHashMap<String, String>> getImageDetailsByStoreVisit(int projectId, String storeId, String taskId){
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImageDetailsByStoreVisit----------------\n");
        String sql = "SELECT isn.imageUUID,isn.origWidth,isn.origHeight,isn.newWidth,isn.newHeight,isn.imageRotation,isn.pixelsPerInch,isn.categoryId,isn.sequenceNumber\n" + 
        		" FROM ImageStoreNew isn, ProjectRepQuestions prq\n" + 
        		" WHERE isn.projectId = ? and isn.storeId = ? and isn.taskId = ?\n" + 
        		"      AND isn.projectId = prq.projectId AND isn.questionId = prq.questionId AND prq.skipImageAnalysis = 0\n" + 
        		" ORDER BY sequenceNumber";

        List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();

        Connection conn = null;

        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("imageUUID", rs.getString("imageUUID"));
                map.put("origWidth", ifNullToEmpty(rs.getString("origWidth")));
                map.put("origHeight", ifNullToEmpty(rs.getString("origHeight")));
                map.put("newWidth", ifNullToEmpty(rs.getString("newWidth")));
                map.put("newHeight", ifNullToEmpty(rs.getString("newHeight")));
                map.put("imageRotation", ifNullToEmpty(rs.getString("imageRotation")));
                map.put("pixelsPerInch", ifNullToEmpty(rs.getString("pixelsPerInch")));
                map.put("sequenceNumber", rs.getString("sequenceNumber"));
                map.put("categoryId", rs.getString("categoryId"));

                resultList.add(map);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getImageDetailsByStoreVisit----------------\n");
            return resultList;
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
    public void updateLinearFootageInProjectStoreResult(int projectId,
                                                        String storeId,String taskId, String linearFootage,
                                                        String stitchedImagePath, String subCategoryHeatMapPath) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts updateLinearFootageInProjectStoreResult LinearFootage: {}, stitchedImagePath: {}",linearFootage, stitchedImagePath);
        String updatePreviewImageSql = "UPDATE ProjectStoreResult SET linearFootage = ?, stitchedImagePath = ?, subCategoryHeatMapPath = ? WHERE projectId = ? and storeId = ? and taskId= ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updatePreviewImageSql);
            ps.setString(1, linearFootage);
            ps.setString(2, stitchedImagePath);
            ps.setString(3, subCategoryHeatMapPath);
            ps.setInt(4, projectId);
            ps.setString(5, storeId);
            ps.setString(6, taskId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateLinearFootageInProjectStoreResult----------------\n");

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

    public String sumOfOosFacings(int projectId,String storeId, String taskId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts sumOfOosFacings::projectId={}", projectId);
        String sql = "select coalesce(sum(facing),0) as sumOosFacing from ProjectStoreData \n" + 
        		"where projectId=? and storeId=? and taskId=? \n" + 
        		"    and upc like ('50\\_%') and skuTypeId in ('1','2','3');";
        Connection conn = null;
        String sumOosFacing = "0";
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
            	sumOosFacing = rs.getString("sumOosFacing");
                if ( StringUtils.isBlank(sumOosFacing) ) { sumOosFacing = "0"; }
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends sumOfOosFacings sumOosFacing = {}", sumOosFacing);

            return sumOosFacing;
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

    private void updateStoreResultParamtersForProjectStoreVisit(int projectId, String storeId, String taskId, Map<String,String> parameters) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts updateStoreResultParamtersForProjectStoreVisit----------------\n");
        String updateSql = "UPDATE ProjectStoreResult SET countDistinctUpc = ?, sumFacing = ?, sumUpcConfidence = ?, "
        		+ " resultCode = ?, status = ?, resultComment = ?, countMissingUPC = ?, percentageOsa = ?, reviewStatus = ?"
        		+ " WHERE projectId = ? and storeId = ? and taskId= ?";
        
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateSql);
            ps.setInt(1, Integer.parseInt(parameters.get("countDistinctUpc")));
            ps.setInt(2, Integer.parseInt(parameters.get("sumFacing")));
            ps.setBigDecimal(3, new BigDecimal(parameters.get("sumUpcConfidence")));
            ps.setString(4, parameters.get("resultCode"));
            ps.setString(5, parameters.get("status"));
            ps.setString(6, parameters.get("resultComment"));
            ps.setString(7, parameters.get("countMissingUPC"));
            ps.setString(8, parameters.get("percentageOsa"));
            ps.setString(9, parameters.get("status"));

            ps.setInt(10, projectId);
            ps.setString(11, storeId);
            ps.setString(12, taskId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateStoreResultParamtersForProjectStoreVisit----------------\n");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);

        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
                }
            }
        }
    }

    @Override
    public Map<String, String> getProjectStoreResultByCustomerCodeAndProjectId(int projectId, String storeId, String taskId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreResultByCustomerCodeAndProjectId----------------\n");
        String sql = "SELECT projectId, storeId, taskId, imageURL, stitchedImagePath, subCategoryHeatMapPath FROM ProjectStoreResult where projectId = ? and storeId = ? and taskId = ?";

        Map<String, String> response = new HashMap<String, String>();
        Connection conn = null;

        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                response.put("projectId", rs.getInt("projectId")+"");
                response.put("storeId", ifNullToEmpty(rs.getString("storeId")));
                response.put("taskId", ifNullToEmpty(rs.getString("taskId")));
                response.put("imageURL", ifNullToEmpty(rs.getString("imageURL")));
                response.put("stitchedImagePath", ifNullToEmpty(rs.getString("stitchedImagePath")));
                response.put("subCategoryHeatMapPath", ifNullToEmpty(rs.getString("subCategoryHeatMapPath")));
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreResultByCustomerCodeAndProjectId----------------\n");
            return response;
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
                }
            }
        }
    }

    @Override
    public void addImageAnalysisNew(String newUpc, String imageUUID, String leftTopX, String leftTopY,
                                    String shelfLevel, int projectId,
                                    String storeId, String height, String width,
                                    String price, String promotion, String compliant) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts AddImageAnalysisNew----------------\n");
        String imageAnalysisNewSql = "insert into ImageAnalysisNew (imageUUID, upc, leftTopX, leftTopY, shelfLevel, projectId, storeId, height, width, taskId, dateId, upcConfidence, price, priceConfidence, promotion, compliant ) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )";

        ImageStore imageStore = findByImageUUId(imageUUID);

        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(imageAnalysisNewSql);
            ps.setString(1, imageUUID);
            ps.setString(2, newUpc);
            ps.setString(3, leftTopX);
            ps.setString(4, leftTopY);
            ps.setString(5, shelfLevel);
            ps.setInt(6, projectId);
            ps.setString(7, storeId);
            ps.setString(8, height);
            ps.setString(9, width);
            ps.setString(10, imageStore.getTaskId());
            ps.setString(11, imageStore.getDateId());
            ps.setString(12, MAX_UPC_CONFIDENCE);
            ps.setString(13, price);
            ps.setString(14, "1");
            ps.setString(15, promotion);
            ps.setString(16, compliant);

            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends AddImageAnalysisNew----------------\n");

        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION [" + e.getMessage() + " , " + e);
                }
            }
        }
    }

    @Override
    public void updateUPCForImageAnalysis(String newUpc,String id, String imageUUID, String leftTopX, String leftTopY, String shelfLevel,
    		String price, String promotion, String compliant) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts updateUPCForImageAnalysis----------------\n");
        String updateImageAnalysisNewSql = "UPDATE ImageAnalysisNew SET upc = ?, shelfLevel = ?, price = ?, promotion = ?, compliant = ?, upcConfidence = ?  WHERE id = ? and imageUUID = ? and leftTopX = ? and leftTopY= ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateImageAnalysisNewSql);
            ps.setString(1, newUpc);
            ps.setString(2, shelfLevel);
            ps.setString(3, price);
            ps.setString(4, promotion);
            ps.setString(5, compliant);
            ps.setString(6, MAX_UPC_CONFIDENCE);
            ps.setInt(7, Integer.valueOf(id));
            ps.setString(8, imageUUID);
            ps.setString(9, leftTopX);
            ps.setString(10, leftTopY);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateUPCForImageAnalysis----------------\n");

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
    
    @Override
    public void updateUPCForImageAnalysis(String imageUUID, String id, String newUpc ) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts updateUPCForImageAnalysis----------------\n");
        String updateImageAnalysisNewSql = "UPDATE ImageAnalysisNew SET upc = ? WHERE id = ? and imageUUID = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateImageAnalysisNewSql);
            ps.setString(1, newUpc);
            ps.setString(2, id);
            ps.setString(3, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateUPCForImageAnalysis----------------\n");

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

    @Override
    public void updateProjectImagePath(int projectId, String imagePath) {

        LOGGER.info("---------------ProcessImageDaoImpl Starts updateProjectImagePath----------------\n");
        String updateProjectSql = "UPDATE Project SET imagePath = ? WHERE id = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(updateProjectSql);
            ps.setString(1, imagePath);
            ps.setInt(2, projectId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateProjectImagePath----------------\n");

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

    @Override
    public Map<String, String> getProjectByCustomerCodeAndCustomerProjectId(int projectId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectByCustomerCodeAndCustomerProjectId----------------\n");
        String sql = "SELECT * FROM Project where id = ?";

        Map<String, String> response = new HashMap<String, String>();
        Connection conn = null;

        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                response.put("projectId", ifNullToEmpty(rs.getInt("id")+""));
                response.put("projectName", rs.getString("projectName"));
                response.put("imagePath", ifNullToEmpty(rs.getString("imagePath")));
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectByCustomerCodeAndCustomerProjectId----------------\n");
            return response;
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
	public void updateImageResultCodeToUnapprovedForNotEnoughTimeResponse(int projectId, int parentProjectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeToUnapprovedForNotEnoughTimeResponse::"
				+ "projectId={}, parentProjectId={}", projectId, parentProjectId);
        String sql = "UPDATE ImageStoreNew SET imageResultCode = '0'"
        		+ " WHERE imageReviewStatus = '1' AND imageStatus = 'done' AND projectId = ? "
        		+ " AND resultUploaded = '0' AND imageResultCode IN ('1','2','3','4','5')" 
        		+ " AND taskId IN (SELECT DISTINCT(taskId) FROM ProjectRepResponses" 
        		+ "  WHERE projectId = ? AND repResponse = 'NO' ) ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, parentProjectId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeToUnapprovedForNotEnoughTimeResponse----------------\n");


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
	
	@Override
	public void updateImageResultCodeToUnapprovedForFeedbackQuestionResponse(int projectId, String feedbackQuestionId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageResultCodeToUnapprovedForFeedbackQuestionResponse::"
				+ "projectId={}, feedbackQuestionId={}", projectId, feedbackQuestionId);
        String sql = "update ImageStoreNew AS A " + 
        		"INNER JOIN (select imageUUID from ImageStoreNew A, " + 
        		"(select projectId,storeId,taskId,questionId,repResponse from ProjectRepResponses where projectId=? and questionId=? and length(repResponse)>10) B " + 
        		"where A.projectId=? and A.imageResultCode in ('1','2','3','4') and A.imageStatus='done' and A.resultUploaded='0' and A.projectId=B.projectId AND A.storeId=B.storeId AND A.taskId = B.taskId " + 
        		") AS B " + 
        		"ON A.imageUUID = B.imageUUID " + 
        		"SET A.imageResultCode='0',A.oldComments=A.imageResultComments,A.imageResultComments='Rep has provided comments',A.resultUploaded='0',A.imageStatus='done', A.imageReviewStatus='1'";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, feedbackQuestionId);
            ps.setInt(3, projectId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageResultCodeToUnapprovedForFeedbackQuestionResponse----------------\n");


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

	@Override
	public List<Map<String, Object>> getQuestionsBasedDetections(int projectId) {
		 LOGGER.debug("---------------ProcessImageDaoImpl Starts getQuestionsBasedDetections::projectId={}", projectId);
	     List<Map<String,Object>> result= new ArrayList<Map<String,Object>>();
	     
		 String storesQuery = "select img.storeId,img.taskId, sm.retailerStoreId, sm.retailer,sm.street,sm.city,sm.stateCode,sm.zip" + 
		 		"	from ImageStoreNew img,StoreMaster sm,ProjectStoreResult psr " + 
		 		"	where img.projectId=? and img.storeId=sm.storeId " + 
		 		"	and img.projectId=psr.projectId and img.storeId=psr.storeId and img.taskId=psr.taskId " + 
		 		"	and psr.status = '1' group by img.storeId, img.taskId order by img.storeId, img.taskId";
		 
		 String imagesQuery = "select imageUUID,questionId,imageURL,agentId,dateId from ImageStoreNew where projectId=? and storeId=? and taskId=? order by questionId";
		 
		 String upcsQuery = "select distinct(upc) from ImageAnalysisNew where imageUUID in (::images)";
		 
		 Connection conn = null;

	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement storesPs = conn.prepareStatement(storesQuery);
        		PreparedStatement imagesPs = conn.prepareStatement(imagesQuery);

	            storesPs.setInt(1, projectId);
	            ResultSet storesRs = storesPs.executeQuery();
	            while (storesRs.next()) {
	            	Map<String,Object> oneStore = new HashMap<String,Object>();
	            	oneStore.put("storeId", storesRs.getString("storeId"));
	            	oneStore.put("taskId", storesRs.getString("taskId"));
	            	oneStore.put("retailerStoreId", storesRs.getString("retailerStoreId"));
	            	oneStore.put("retailer", storesRs.getString("retailer"));
	            	oneStore.put("street", storesRs.getString("street"));
	            	oneStore.put("city", storesRs.getString("city"));
	            	oneStore.put("stateCode", storesRs.getString("stateCode"));
	            	oneStore.put("zip", storesRs.getString("zip"));
	            	result.add(oneStore);
	            }
	            storesRs.close();
	            storesPs.close();
	            
	            for ( Map<String, Object> store : result ) {
	            	String storeId = (String) store.get("storeId");
	            	String taskId = (String) store.get("taskId");
	            	
	            	Map<String,Object> questionDetectionMap = new LinkedHashMap<String,Object>();
            		Map<String,List<String>> questions = new LinkedHashMap<String,List<String>>();
            		Map<String,String> questionURLAgentDateMap = new HashMap<String,String>();
            		
            		imagesPs.setInt(1, projectId);
            		imagesPs.setString(2, storeId);
            		imagesPs.setString(3, taskId);
            		ResultSet imagesRs = imagesPs.executeQuery();
            		while (imagesRs.next()) {
            			String imageUUID=imagesRs.getString("imageUUID");
            			String questionId= imagesRs.getString("questionId");
            			String imageURL = imagesRs.getString("imageURL");
            			String agentId = imagesRs.getString("agentId");
            			String dateId = imagesRs.getString("dateId");

            			if ( questions.get(questionId) == null) {
            				questions.put(questionId, new ArrayList<String>());
            			}
            			
            			questions.get(questionId).add(imageUUID);
            			
            			String value = agentId+"#"+dateId+"#"+imageURL;
            			questionURLAgentDateMap.put(questionId, value);
            		}
            		imagesRs.close();
            		
	            	for( String question : questions.keySet()) {
	            		List<String> imageUUIDs = questions.get(question);
	            		List<String> upcs = new ArrayList<String>();
	    	            if (!imageUUIDs.isEmpty()) {
	    	            	String step1 = StringUtils.join(imageUUIDs, "\", \"");// Join with ", "
	    	            	String images = StringUtils.wrap(step1, "\"");// Wrap step1 with "
	    	            	String upcsQueryForThisStoreQuestion = upcsQuery.replaceAll("::images", images);
	    	            	PreparedStatement upcsPs = conn.prepareStatement(upcsQueryForThisStoreQuestion);
	    	            	ResultSet upcsRs = upcsPs.executeQuery();
	    	            	while (upcsRs.next()) {
	    	            		upcs.add(upcsRs.getString("upc"));
	    	            	}
	    	            	upcsRs.close();
	    	            	upcsPs.close();
	    	            }
	    	            Map<String,Object> detections = new HashMap<String,Object>();
	    	            detections.put("metaInfo", questionURLAgentDateMap.get(question));
	    	            detections.put("products", upcs);
	    	            questionDetectionMap.put(question, detections);
	            	}
	            	
	            	store.put("questions", questionDetectionMap);
	            }
	            imagesPs.close();
	            
	            LOGGER.debug("---------------ProcessImageDaoImpl Ends getQuestionsBasedDetections :: respone = {}----------------\n", result);
	            return result;
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
	public List<String> getListSubCategoriesNew(String customerCode, int parentProjectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getListSubCategoriesNew::customer code={},parent projectId={}", customerCode, parentProjectId);
        String sql = "SELECT" + 
        		"    DISTINCT(pm.PRODUCT_SUB_TYPE)" + 
        		" FROM ProjectUpc pu, ProductMaster pm " + 
        		" WHERE pu.projectId IN (" + 
        		"        SELECT project.id FROM Project project " + 
        		"        INNER JOIN (" + 
        		"        	SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" + 
        		"        ) customerMappedProjects " + 
        		"        ON ( project.id = customerMappedProjects.projectId )" + 
        		"        WHERE project.parentProjectId = ? AND project.status = '1'" + 
        		"    ) AND pu.skuTypeId in ('1','2','3') AND pu.upc = pm.upc " +
        		"    AND pm.PRODUCT_SUB_TYPE IS NOT NULL AND pm.PRODUCT_SUB_TYPE != ''" +
        		" ORDER BY PRODUCT_SUB_TYPE";

        Connection conn = null;
        try {
            List<String> subCategoryList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, parentProjectId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                subCategoryList.add(rs.getString("PRODUCT_SUB_TYPE"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListSubCategoriesNew subCategoryList = {}", subCategoryList);

            return subCategoryList;
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
	public List<String> getListModularsNew(String customerCode, int parentProjectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getListSubCategoriesNew::customer code={},parent projectId={}", customerCode, parentProjectId);
        String sql = "SELECT" + 
        		"    DISTINCT(psr.linearFootage) AS modular" + 
        		" FROM ProjectStoreResult psr " + 
        		" WHERE psr.projectId IN (" + 
        		"        SELECT project.id FROM Project project " + 
        		"        INNER JOIN (" + 
        		"        	SELECT projectId FROM CustomerCodeProjectMap WHERE customerCode = ?" + 
        		"        ) customerMappedProjects " + 
        		"        ON ( project.id = customerMappedProjects.projectId )" + 
        		"        WHERE project.parentProjectId = ? AND project.status = '1'" + 
        		"    ) AND psr.status = '1'" + 
        		" ORDER BY CAST(psr.linearFootage AS UNSIGNED)";

        Connection conn = null;
        try {
            List<String> modularsList =new ArrayList<String>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, customerCode);
            ps.setInt(2, parentProjectId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	modularsList.add(rs.getString("modular"));
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListModularsNew modularsList = {}", modularsList);

            return modularsList;
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
	public Map<String,List<String>> getListRetailersAndStoreFormats(List<String> childProjectIds) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getListRetailersAndStoreFormats::child project ids={}", childProjectIds);
        String sql = "SELECT" + 
        		"    sm.retailer as retailerName," + 
        		"    sm.storeFormat as storeFormat" + 
        		" FROM " + 
        		"    ProjectStoreResult psr, StoreMaster sm" + 
        		" WHERE \n" + 
        		"     psr.projectId in (::projectIds) AND" + 
        		"     psr.status = '1' AND" + 
        		"     psr.storeId = sm.storeId" + 
        		" GROUP BY" + 
        		"    retailerName, storeFormat" + 
        		" ORDER BY retailerName, storeFormat";

        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
        Connection conn = null;
        try {
            Map<String,List<String>> resultMap =new LinkedHashMap<String,List<String>>();
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	String retailer = rs.getString("retailerName");
            	String storeFormat = rs.getString("storeFormat");

            	if (resultMap.get(retailer) == null ) {
            		resultMap.put(retailer, new ArrayList<String>());
            	}
            	
            	if ( StringUtils.isNotBlank(storeFormat) ) {
                	resultMap.get(retailer).add(storeFormat);
            	}
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getListRetailersAndStoreFormats map = {}", resultMap);

            return resultMap;
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
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllModulars(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBrandSharesByBrandForAllModulars::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, subCategory= {}, storeFormat= {}, retailer= {}",projectId,month,waveId,childProjectIds,brandNameToFilter,subCategoryToFilter,
				storeFormatToFilter, retailerToFilter);
		
		String sqlFileName = "getProjectBrandSharesByBrandForAllModulars.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategoryToFilter.equals("all") ? "%" : subCategoryToFilter;
    	String storeFormatFilter = storeFormatToFilter.equals("all") ? "%" : storeFormatToFilter;
    	String retailerFilter = retailerToFilter.equals("all") ? "%" : retailerToFilter;
        
        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5,storeFormatFilter);
            ps.setString(6,retailerFilter);
            ps.setString(7, subCategoryFilter);
            ps.setString(8, brandNameToFilter);
            ps.setString(9, month+"%");
            ps.setString(10, waveId);
            ps.setString(11, month+"%");
            ps.setString(12, waveId);
            ps.setString(13,storeFormatFilter);
            ps.setString(14,retailerFilter);
            ps.setString(15, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("modular", rs.getString("MODULAR"));
                map.put("stores", rs.getString("STORES"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                //map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                map.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBrandSharesByBrandForAllModulars----------------\n");
            return result;
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
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllRetailers(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBrandSharesByBrandForAllRetailers::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, subCategory= {}, storeFormat= {}",projectId,month,waveId,childProjectIds,brandNameToFilter,subCategoryToFilter, storeFormatToFilter);
		
        String sqlFileName = "getProjectBrandSharesByBrandForAllRetailers.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategoryToFilter.equals("all") ? "%" : subCategoryToFilter;
    	String storeFormatFilter = storeFormatToFilter.equals("all") ? "%" : storeFormatToFilter;

        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5, storeFormatFilter);
            ps.setString(6, subCategoryFilter);
            ps.setString(7, brandNameToFilter);
            ps.setString(8, month+"%");
            ps.setString(9, waveId);
            ps.setString(10, month+"%");
            ps.setString(11, waveId);
            ps.setString(12, storeFormatFilter);
            ps.setString(13, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("retailerName", rs.getString("RETAILERNAME"));
                map.put("stores", rs.getString("STORES"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                //map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                map.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBrandSharesByBrandForAllRetailers----------------\n");
            return result;
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
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllStoreFormats(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String retailerToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBrandSharesByBrandForAllStoreFormats::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, subCategory= {}, retailer= {}",projectId,month,waveId,childProjectIds,brandNameToFilter,subCategoryToFilter, retailerToFilter);
		
		String sqlFileName = "getProjectBrandSharesByBrandForAllStoreFormats.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategoryToFilter.equals("all") ? "%" : subCategoryToFilter;
    	String retailerFilter = retailerToFilter.equals("all") ? "%" : retailerToFilter;
    	String storeFormatFilter = "%";
        
        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5,storeFormatFilter);
            ps.setString(6,retailerFilter);
            ps.setString(7, subCategoryFilter);
            ps.setString(8, brandNameToFilter);
            ps.setString(9, month+"%");
            ps.setString(10, waveId);
            ps.setString(11, month+"%");
            ps.setString(12, waveId);
            ps.setString(13,storeFormatFilter);
            ps.setString(14,retailerFilter);
            ps.setString(15, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("storeFormat", rs.getString("STOREFORMAT"));
                map.put("stores", rs.getString("STORES"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                //map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                map.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBrandSharesByBrandForAllStoreFormats----------------\n");
            return result;
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
	public List<Map<String, String>> getProjectBrandSharesByBrandForAllSubCategories(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String storeFormatToFilter, String retailerToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectBrandSharesByBrandForAllSubCategories::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, storeFormat= {}, retailerFilter= {}",projectId,month,waveId,childProjectIds,brandNameToFilter, storeFormatToFilter, retailerToFilter);
		
		String sqlFileName = "getProjectBrandSharesByBrandForAllSubCategories.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = "%";
    	String storeFormatFilter = storeFormatToFilter.equals("all") ? "%" : storeFormatToFilter;
    	String retailerFilter = retailerToFilter.equals("all") ? "%" : retailerToFilter;

        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5, storeFormatFilter);
            ps.setString(6, retailerFilter);
            ps.setString(7, subCategoryFilter);
            ps.setString(8, brandNameToFilter);
            ps.setString(9, month+"%");
            ps.setString(10, waveId);
            ps.setString(11, month+"%");
            ps.setString(12, waveId);
            ps.setString(13, storeFormatFilter);
            ps.setString(14, retailerFilter);
            ps.setString(15, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
                map.put("subCategory", rs.getString("SUBCATEGORY"));
                map.put("stores", rs.getString("STORES"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                //map.put("brandTotalUpc", rs.getString("BRAND_TOTAL_UPC"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                map.put("averageUpcPerStore", rs.getString("AVG_UPC_PER_STORE"));
                map.put("averageFacingPerStore", rs.getString("AVG_FACING_PER_STORE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectBrandSharesByBrandForAllSubCategories----------------\n");
            return result;
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
	public List<Map<String, String>> getProjectAllStoreShareOfShelfByBrand(int projectId, List<String> childProjectIds,
			String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreShareOfShelfByBrand::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, subCategory= {}, storeFormat= {}, retailer= {}",projectId,month,waveId,childProjectIds,brandNameToFilter,
				subCategoryToFilter,storeFormatToFilter,retailerToFilter);
		
        String sqlFileName = "getProjectAllStoreShareOfShelfByBrand.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategoryToFilter.equals("all") ? "%" : subCategoryToFilter;
    	String storeFormatFilter = storeFormatToFilter.equals("all") ? "%" : storeFormatToFilter;
    	String retailerFilter = retailerToFilter.equals("all") ? "%" : retailerToFilter;

        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5, storeFormatFilter);
            ps.setString(6, retailerFilter);
            ps.setString(7, brandNameToFilter);
            ps.setString(8, subCategoryFilter);
            ps.setString(9, month+"%");
            ps.setString(10, waveId);
            ps.setString(11, month+"%");
            ps.setString(12, waveId);
            ps.setString(13, storeFormatFilter);
            ps.setString(14, retailerFilter);
            ps.setString(15, subCategoryFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("projectId", rs.getString("projectId"));
                map.put("taskId", rs.getString("taskId"));
                map.put("storeId", rs.getString("storeId"));
                map.put("visitDateId", rs.getString("visitDateId"));
                map.put("imageUUID", rs.getString("imageUUID"));
                map.put("agentId", rs.getString("agentId"));
                map.put("processedDate", rs.getString("processedDate"));
                map.put("retailerStoreId", rs.getString("retailerStoreId"));
                map.put("retailer", rs.getString("retailer"));
                map.put("street", rs.getString("street"));
                map.put("city", rs.getString("city"));
                map.put("state", rs.getString("state"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreShareOfShelfByBrand----------------\n");
            return result;
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
	public List<Map<String, String>> getProjectAllStoreShareOfShelf(int projectId, List<String> childProjectIds, String waveId) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreShareOfShelf::projectId= {}, waveId= {}, childProjectIds= {}",
				projectId,waveId,childProjectIds);
		
        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		
        String sqlFileName = "getProjectAllStoreShareOfShelf.sql";
        String sql = ConverterUtil.getResourceFromClasspath(sqlFileName, "/queries/");
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
        Connection conn = null;
        List<Map<String,String>> result=new ArrayList<Map<String,String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, waveId);
            ps.setString(2, waveId);
            ps.setString(3, waveId);
            ps.setString(4, waveId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("projectId", rs.getString("projectId"));
                map.put("taskId", rs.getString("taskId"));
                map.put("storeId", rs.getString("storeId"));
                String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
	            if ( !visitDate.isEmpty() ) {
	               try {
	            	   	visitDate = outSdf.format(inSdf.parse(visitDate));
	               } catch (ParseException e) {
                       LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
	               }
	            }
            	map.put("visitDate", visitDate);
            	map.put("OSAPercentage", rs.getString("OSAPercentage"));
                map.put("linearFootage", rs.getString("linearFootage"));
                map.put("retailerStoreId", rs.getString("retailerStoreId"));
                map.put("retailer", rs.getString("retailer"));
                map.put("street", rs.getString("street"));
                map.put("city", rs.getString("city"));
                map.put("state", rs.getString("state"));
                map.put("zip", rs.getString("zip"));
                map.put("storeFormat", ifNullToEmpty(rs.getString("storeFormat")));
                map.put("brandName", rs.getString("brandName"));
                map.put("upcCount", rs.getString("BRAND_UPC"));
                map.put("facingCount", rs.getString("BRAND_FACING"));
                map.put("shareOfUpcs", rs.getString("BRAND_UPC_SHARE"));
                map.put("shareOfFacings", rs.getString("BRAND_FACES_SHARE"));
                result.add(map);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreShareOfShelf----------------\n");
            return result;
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
    public void deleteImageAnalysisNew(String id) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts deleteImageAnalysisNew : id = {}", id);
        String deleteImageAnalysisNewSql = "DELETE FROM ImageAnalysisNew WHERE id = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(deleteImageAnalysisNewSql);
            ps.setInt(1, Integer.valueOf(id));
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends deleteImageAnalysisNew ");

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
	
	@Override
	public List<Map<String, Object>> getProjectStoreCountForAllProductsAllModulars(int projectId,
			List<String> childProjectIds, String month, String waveId, String brandNameToFilter, String subCategoryToFilter, String storeFormatToFilter, String retailerToFilter) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreCountForAllProductsAllModulars::projectId= {}, month= {}, waveId= {}, childProjectIds= {}, "
				+ "brandName= {}, subCategory= {}, storeFormat= {}, retailer= {}",projectId,month,waveId,childProjectIds,brandNameToFilter,subCategoryToFilter,
				storeFormatToFilter, retailerToFilter);
		
		String sql = "SELECT\n" + 
        		"   A.MODULAR,\n" + 
        		"   B.UPC,\n" + 
        		"   B.productName,\n" + 
        		"   COUNT(*) AS STORES\n" + 
        		"FROM\n" + 
        		"   (\n" + 
        		"      SELECT\n" + 
        		"         PROJECTID,\n" + 
        		"         STOREID,\n" + 
        		"         TASKID,\n" + 
        		"         VISITDATEID,\n" + 
        		"         LINEARFOOTAGE AS MODULAR\n" + 
        		"      FROM\n" + 
        		"         ProjectStoreResult\n" + 
        		"      WHERE\n" + 
        		"         STATUS = 1 AND  PROJECTID IN (::projectIds) and visitDateId like ? and COALESCE(waveId,'') like ? AND \n" + 
        		"         (PROJECTID, STOREID, VISITDATEID) IN\n" + 
        		"         (\n" + 
        		"            SELECT\n" + 
        		"               psr.PROJECTID,\n" + 
        		"               psr.STOREID,\n" + 
        		"               MAX(psr.VISITDATEID)\n" + 
        		"            FROM\n" + 
        		"               ProjectStoreResult psr, StoreMaster sm\n" + 
        		"            WHERE\n" + 
        		"               psr.STATUS = '1' AND psr.PROJECTID IN (::projectIds) and psr.visitDateId like ? and COALESCE(psr.waveId,'') like ? \n" +
        		"               and COALESCE(sm.storeFormat,'') like ? and COALESCE(sm.retailer,'') like ? and psr.storeId = sm.storeId \n" + 
        		"            GROUP BY\n" + 
        		"               psr.PROJECTID,\n" + 
        		"               psr.STOREID\n" + 
        		"         )\n" + 
        		"   ) A,\n" + 
        		"   ProjectStoreData B,\n" + 
        		"   (\n" + 
        		"       SELECT * from ProductMaster WHERE PRODUCT_SUB_TYPE LIKE ? AND BRAND_NAME = ?\n" + 
        		"   ) C \n" + 
        		"WHERE\n" + 
        		"   B.STOREID           = A.STOREID           AND\n" + 
        		"   B.TASKID            = A.TASKID            AND\n" + 
        		"   B.PROJECTID      = A.PROJECTID            AND\n" + 
        		"   B.SKUTYPEID      IN ('1','2','3')         AND\n" + 
        		"   B.UPC = C.UPC\n" + 
        		"GROUP BY\n" + 
        		"   A.MODULAR,B.UPC\n" + 
        		"ORDER BY\n" + 
        		"    A.MODULAR ASC, STORES DESC";
        
        String step1 = StringUtils.join(childProjectIds, "\", \"");// Join with ", "
    	String listOfProjectIdsToFilter = StringUtils.wrap(step1, "\"");// Wrap step1 with "
    	sql = sql.replaceAll("::projectIds", listOfProjectIdsToFilter);
    	
    	String subCategoryFilter = subCategoryToFilter.equals("all") ? "%" : subCategoryToFilter;
    	String storeFormatFilter = storeFormatToFilter.equals("all") ? "%" : storeFormatToFilter;
    	String retailerFilter = retailerToFilter.equals("all") ? "%" : retailerToFilter;

        Connection conn = null;
        Map<String,Map<String,Object>> resultMap = new LinkedHashMap<String,Map<String,Object>>();
        List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, month+"%");
            ps.setString(2, waveId);
            ps.setString(3, month+"%");
            ps.setString(4, waveId);
            ps.setString(5,storeFormatFilter);
            ps.setString(6,retailerFilter);
            ps.setString(7, subCategoryFilter);
            ps.setString(8, brandNameToFilter);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String modular = rs.getString("MODULAR");
                
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("modular", modular);
                Map<String,String> data = new HashMap<String,String>();
                data.put("upc", rs.getString("UPC"));
                data.put("productName", rs.getString("productName"));
                data.put("stores", rs.getString("STORES"));
                
                if ( resultMap.get(modular) == null ) {
                	resultMap.put(modular, new HashMap<String,Object>());
                	resultMap.get(modular).put("modular", modular);
                	resultMap.get(modular).put("productStoreShare", new ArrayList<Map<String,String>>());
                }
                ((List<Map<String,String>>)(resultMap.get(modular).get("productStoreShare"))).add(data);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreCountForAllProductsAllModulars----------------\n");
            for(String modular : resultMap.keySet()) {
            	resultList.add(resultMap.get(modular));
            }
            return resultList;
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
	public Map<String,List<String>> getDailyImageErrorStats() {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getDailyImageErrorStats---------");
		
		String sql = "SELECT " + 
				"    CONCAT(DATE_FORMAT(processedDate, '%d-%m-%Y'),'#',projectId,'#',categoryId) AS dateProjectCategory," + 
				"    imageUUID" + 
				" FROM ImageStoreNew " + 
				" WHERE processedDate > ( CURDATE() - INTERVAL 1 DAY ) " + 
				"    AND imageStatus = 'error'" + 
				" ORDER BY dateProjectCategory";
        
        Connection conn = null;
        Map<String,List<String>> dateProjectCategoryImageMap = new LinkedHashMap<String,List<String>>();

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String dateProjectCategory = rs.getString("dateProjectCategory");
                String imageUUID = rs.getString("imageUUID");
                if ( dateProjectCategoryImageMap.get(dateProjectCategory) == null ) {
                	dateProjectCategoryImageMap.put(dateProjectCategory, new ArrayList<String>());
                }
                dateProjectCategoryImageMap.get(dateProjectCategory).add(imageUUID);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getDailyImageErrorStats----------------\n");
            return dateProjectCategoryImageMap;
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
	public void saveProjectStoreScores(int projectId, String storeId, String taskId,
			List<Map<String,Object>> scores, List<Map<String,Object>> componentScores, List<Map<String,Object>> componentCriteriaScores) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts saveProjectStoreScores------------------");
        String deleteCriteriaScoresSql = "DELETE FROM ProjectStoreComponentCriteriaScore WHERE projectId=? AND storeId=? AND taskId=?";
        String deleteComponentScoresSql = "DELETE FROM ProjectStoreComponentScore WHERE projectId=? AND storeId=? AND taskId=?";
        String deleteHighLevelScoresSql = "DELETE FROM ProjectStoreScore WHERE projectId=? AND storeId=? AND taskId=?";

        String insertHighLevelScoresSql = "INSERT INTO ProjectStoreScore (projectId,storeId,taskId,scoreId,score,scoreGroupId) VALUES (?,?,?,?,?,?)";
        String insertComponentScoresSql = "INSERT INTO ProjectStoreComponentScore (projectId,storeId,taskId,scoreId,componentScoreId,componentScore,componentScoreComment,componentScoreAction)"
        		+ " VALUES (?,?,?,?,?,?,?,?)";
        String insertCriteriaScoresSql = "INSERT INTO ProjectStoreComponentCriteriaScore (projectId,storeId,taskId,scoreId,componentScoreId,groupId,groupSequenceNumber,result,score)"
        		+ " VALUES (?,?,?,?,?,?,?,?,?)";

        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            
            //Delete all entries for this store visit
            PreparedStatement deleteCriteriaScoresPs = conn.prepareStatement(deleteCriteriaScoresSql);
            deleteCriteriaScoresPs.setInt(1, projectId);
            deleteCriteriaScoresPs.setString(2, storeId);
            deleteCriteriaScoresPs.setString(3, taskId);
            
            PreparedStatement deleteComponentScoresPs = conn.prepareStatement(deleteComponentScoresSql);
            deleteComponentScoresPs.setInt(1, projectId);
            deleteComponentScoresPs.setString(2, storeId);
            deleteComponentScoresPs.setString(3, taskId);
            
            PreparedStatement deleteHighLevelScoresPs = conn.prepareStatement(deleteHighLevelScoresSql);
            deleteHighLevelScoresPs.setInt(1, projectId);
            deleteHighLevelScoresPs.setString(2, storeId);
            deleteHighLevelScoresPs.setString(3, taskId);
            
            deleteCriteriaScoresPs.execute();
            deleteComponentScoresPs.execute();
            deleteHighLevelScoresPs.execute();
            
            PreparedStatement insertHighLevelScoresPs = conn.prepareStatement(insertHighLevelScoresSql);
            insertHighLevelScoresPs.setInt(1, projectId);
            insertHighLevelScoresPs.setString(2, storeId);
            insertHighLevelScoresPs.setString(3, taskId);

            PreparedStatement insertComponentScoresPs = conn.prepareStatement(insertComponentScoresSql);
            insertComponentScoresPs.setInt(1, projectId);
            insertComponentScoresPs.setString(2, storeId);
            insertComponentScoresPs.setString(3, taskId);
            
            PreparedStatement insertCriteriaScoresPs = conn.prepareStatement(insertCriteriaScoresSql);
            insertCriteriaScoresPs.setInt(1, projectId);
            insertCriteriaScoresPs.setString(2, storeId);
            insertCriteriaScoresPs.setString(3, taskId);
            
            //Insert new high level scores
            for(Map<String, Object> score : scores) {
            	insertHighLevelScoresPs.setInt(4, Integer.parseInt(""+score.get("scoreId")));
            	insertHighLevelScoresPs.setString(5, ""+score.get("score"));
            	insertHighLevelScoresPs.setInt(6, Integer.parseInt(""+score.get("scoreGroupId")));
            	insertHighLevelScoresPs.execute();
            }
            
            //Insert new component scores
            for(Map<String, Object> score : componentScores) {
            	insertComponentScoresPs.setInt(4, Integer.parseInt(""+score.get("scoreId")));
            	insertComponentScoresPs.setInt(5, Integer.parseInt(""+score.get("componentScoreId")));
            	insertComponentScoresPs.setString(6, ""+score.get("componentScore"));
            	insertComponentScoresPs.setString(7, ""+score.get("componentScoreComment"));
            	insertComponentScoresPs.setString(8, ""+score.get("componentScoreAction"));
            	insertComponentScoresPs.addBatch();
            }
            insertComponentScoresPs.executeBatch();
            
            //Insert new criteria scores
            for(Map<String, Object> score : componentCriteriaScores) {
            	insertCriteriaScoresPs.setInt(4, Integer.parseInt(""+score.get("scoreId")));
            	insertCriteriaScoresPs.setInt(5, Integer.parseInt(""+score.get("componentScoreId")));
            	insertCriteriaScoresPs.setInt(6, Integer.parseInt(""+score.get("groupId")));
            	insertCriteriaScoresPs.setInt(7, Integer.parseInt(""+score.get("groupSequenceNumber")));
            	insertCriteriaScoresPs.setString(8, ""+score.get("result"));
            	insertCriteriaScoresPs.setString(9, ""+score.get("score"));
            	insertCriteriaScoresPs.addBatch();
            }
            
            insertCriteriaScoresPs.executeBatch();
            
            conn.commit();
            
            insertCriteriaScoresPs.close();
            insertComponentScoresPs.close();
            insertHighLevelScoresPs.close();
            
            LOGGER.info("---------------ProcessImageDaoImpl Ends saveProjectStoreScores----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
		
	}
	
	@Override
	public Map<String, String> getPhotoQualityReportByStoreVisit(int projectId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getPhotoQualityReportByStoreVisit :: projectId={}",projectId);
		
		Map<String, String> resultMap = new HashMap<String,String>();
		
		String sql = "SELECT " + 
				"    A.storeId, " + 
				"    A.taskId, " + 
				"    ROUND(A.goodPhotoCount / A.photoCount,1) * 100 AS goodPhotosPercentage " + 
				" FROM ( " + 
				" SELECT " + 
				"    storeId, " + 
				"    taskId, " + 
				"    COUNT(*) AS photoCount, " + 
				"    COUNT(IF(imageNotUsable='0',1,NULL)) AS goodPhotoCount " + 
				" FROM " + 
				"    ImageStoreNew " + 
				" WHERE " + 
				"    projectId = ? " + 
				" GROUP BY " + 
				"    storeId, taskId " + 
				" ) A";
		
		Connection conn = null;
		try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	String storeId = rs.getString("storeId");
            	String taskId = rs.getString("taskId");
            	String goodPhotosPercentage = rs.getString("goodPhotosPercentage");
            	resultMap.put(storeId+"#"+taskId, goodPhotosPercentage);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getPhotoQualityReportByStoreVisit----------------\n");
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
		return resultMap;
	}

	@Override
	public void setStoreVisitForAggregation(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts setStoreVisitForAggregation::projectId={} ::storeId={}::taskId={}",projectId, storeId, taskId);
        String sql = "UPDATE ProjectStoreResult SET resultCode='99', status='0' WHERE projectId = ? AND storeId = ? AND taskId = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends setStoreVisitForAggregation----------------\n");

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
	
	@Override
	public void setStoreVisitForStoreAnalysis(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts setStoreVisitForStoreAnalysis::projectId={} ::storeId={}::taskId={}",projectId, storeId, taskId);
        String sql = "UPDATE ProjectStoreResult SET resultCode='999', status='0' WHERE projectId = ? AND storeId = ? AND taskId = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends setStoreVisitForStoreAnalysis----------------\n");

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
	
	@Override
	public void setStoreVisitToStoreAnalysisReceived(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts setStoreVisitToStoreAnalysisReceived::projectId={}::storeId={}::taskId={}",projectId, storeId, taskId);
        String sql = "UPDATE ProjectStoreResult SET resultCode='998', status='0' WHERE projectId = ? AND storeId = ? AND taskId = ? "
        		+ " AND resultCode='999' AND status='0'";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            int recordUpdated = ps.executeUpdate();
            ps.close();
            
            if ( recordUpdated == 0 ) {
            	LOGGER.error("---------------ProcessImageDaoImpl::No agg-waiting records to update for projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
            }
            
            LOGGER.info("---------------ProcessImageDaoImpl Ends setStoreVisitToStoreAnalysisReceived----------------\n");

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
	
	@Override
	public void setStoreVisitToStoreAnalysisFailed(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts setStoreVisitToStoreAnalysisFailed::projectId={}::storeId={}::taskId={}",projectId, storeId, taskId);
        String sql = "UPDATE ProjectStoreResult SET resultCode='997', status='0' WHERE projectId = ? AND storeId = ? AND taskId = ? "
        		+ " AND resultCode='998' AND status='0'";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            int recordUpdated = ps.executeUpdate();
            ps.close();
            
            if ( recordUpdated == 0 ) {
            	LOGGER.error("---------------ProcessImageDaoImpl::No processing-waiting records to update for projectId={}::storeId={}::taskId={}",projectId,storeId,taskId);
            }
            
            LOGGER.info("---------------ProcessImageDaoImpl Ends setStoreVisitToStoreAnalysisFailed----------------\n");

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
	
	@Override
	public void updateImageQualityParams(ImageStore imageToUpdate) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageQualityParams::image={}",imageToUpdate);
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew "
        		+ "SET oldImageNotUsable = imageNotUsable, imageNotUsable = ?, oldImageNotUsableComment = imageNotUsableComment, imageNotUsableComment = ?, lastUpdatedTimestamp = ?"
        		+ " WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageToUpdate.getImageNotUsable());
            ps.setString(2, imageToUpdate.getImageNotUsableComment());
            ps.setString(3, String.valueOf(currTimestamp));
            ps.setString(4, imageToUpdate.getImageUUID());
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageQualityParams----------------\n");
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
	
	@Override
	public void deleteAllDetectionsByImageUUID(String imageUUID) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts deleteAllDetectionsByImageUUID::image={}",imageUUID);
        String sql = "DELETE FROM ImageAnalysisNew WHERE imageUUID = ? ";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageUUID);
            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends deleteAllDetectionsByImageUUID----------------\n");
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
	
	@Override
	public void updateStoreReviewStatus(String projectId, String storeId, String taskId, String reviewStatus, String status) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateStoreReviewStatus::projectId={}, storeId={}, taskId={}, reviewStatus={}, status={}",
				projectId, storeId, taskId, reviewStatus, status);
		
        String sql = "UPDATE ProjectStoreResult set reviewStatus = ? , status = ? WHERE projectId = ? AND storeId = ? AND taskId = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, reviewStatus);
            ps.setString(2, status);
            ps.setString(3, projectId);
            ps.setString(4, storeId);
            ps.setString(5, taskId);

            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateStoreReviewStatus----------------\n");
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
	
	@Override
	public void updateImageReviewStatusByStoreVisit(String projectId, String storeId, String taskId, String imageReviewStatus) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateImageReviewStatusByStoreVisit::projectId={}, storeId={}, taskId={}, imageReviewStatus={}",
				projectId, storeId, taskId, imageReviewStatus);
		
        long currTimestamp = System.currentTimeMillis() / 1000L;
        String sql = "UPDATE ImageStoreNew SET imageReviewStatus = ? , lastUpdatedTimestamp = ? WHERE projectId = ? AND storeId = ? AND taskId = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, imageReviewStatus);
            ps.setString(2, String.valueOf(currTimestamp));
            ps.setString(3, projectId);
            ps.setString(4, storeId);
            ps.setString(5, taskId);

            ps.executeUpdate();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateImageReviewStatusByStoreVisit----------------\n");
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
	
	@Override
	public List<LinkedHashMap<String, String>> getProjectStoreRepResponses(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoreRepResponses:: projectId ={}, storeId ={}, taskId={}", projectId, storeId, taskId);
        String sql = "SELECT response.questionId,question.questionDesc,response.repResponse " + 
        		" FROM ProjectRepResponses response, ProjectRepQuestions question " + 
        		" WHERE response.projectId = ? AND response.storeId = ? AND response.taskId = ? " + 
        		"      AND response.projectId = question.projectId AND response.questionId = question.questionId " + 
        		" ORDER BY response.questionId;";
        Connection conn = null;
        List<LinkedHashMap<String,String>> repResponses = new ArrayList<LinkedHashMap<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3, taskId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	LinkedHashMap<String,String> repResponse = new LinkedHashMap<String,String>();
            	repResponse.put("questionId", ""+rs.getInt("questionId"));
            	repResponse.put("questionDesc", rs.getString("questionDesc"));
            	repResponse.put("repResponse", rs.getString("repResponse"));
            	repResponses.add(repResponse);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoreRepResponses number of responses = {}", repResponses.size());

            return repResponses;
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
	public List<LinkedHashMap<String, String>> getProjectStoresForReview(int projectId, String waveId, String fromDate,
			String toDate, String reviewStatus) {
		 LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectStoresForReview::projectId={}, waveId={}, fromDate={}, toDate={}, reviewStatus={}",
				 projectId,waveId,fromDate,toDate,reviewStatus);
		 
	        String sql = "SELECT result.storeId, store.retailerStoreId, store.retailerChainCode, store.retailer, store.street, store.city, store.stateCode, store.state, store.zip, store.Latitude, store.Longitude, " + 
	    	        "	    result.resultCode, result.resultComment, result.countDistinctUpc, result.sumFacing, result.sumUpcConfidence, result.status, result.imageURL, result.agentId, result.taskId, " + 
	    	        "	    DATE_FORMAT(result.processedDate,'%m/%d/%Y') as processedDate, result.visitDateId, result.imageUUID, result.linearFootage, result.countMissingUpc, result.percentageOsa, result.reviewStatus, " + 
	    	        "	    psgc.criteriaName, psgc.resultColor " + 
	    	        " FROM ProjectStoreResult result   " + 
	    	        " INNER JOIN ( SELECT a.projectId, a.storeId, MAX(concat(a.visitDateId,a.taskId)) as maxVisitTask FROM ProjectStoreResult a WHERE a.projectId = ? AND a.visitDateId BETWEEN ? AND ? AND COALESCE(a.waveId,'') LIKE ? AND reviewStatus LIKE ? GROUP BY a.projectId, a.storeId ) maxresult " + 
	    	        " ON result.projectId = maxresult.projectId AND result.storeId = maxresult.storeId AND concat(result.visitDateId,result.taskId) <=> maxresult.maxVisitTask " + 
	    	        " LEFT JOIN StoreMaster store ON result.storeId = store.storeId " + 
	    	        " LEFT JOIN ProjectStoreGradingCriteria psgc ON result.projectId = psgc.projectId AND result.resultCode = psgc.resultCode " + 
	    	        " WHERE result.projectId=? AND result.visitDateId BETWEEN ? AND ? AND COALESCE(result.waveId,'') LIKE ? AND reviewStatus LIKE ? " + 
	    	        " GROUP BY result.storeId, result.taskId,result.resultCode " + 
	    	        " ORDER BY result.id" ;
	        
	        Connection conn = null;
	        List<LinkedHashMap<String,String>> result=new ArrayList<LinkedHashMap<String,String>>();
	        SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
	        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
	        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
	        
	        String waveFilter = waveId.equals("-9") ? "%" : waveId;
	        String fromDateFilter = fromDate.equals("-9") ? "20000101" : fromDate;
	        String toDateFilter = toDate.equals("-9") ? "99990101" : toDate;
	        String reviewStatusFilter = reviewStatus.equals("-9") ? "%" : reviewStatus;

	        try {
	            conn = dataSource.getConnection();
	            PreparedStatement ps = conn.prepareStatement(sql);
	            ps.setInt(1, projectId);
	            ps.setString(2, fromDateFilter);
	            ps.setString(3, toDateFilter);
	            ps.setString(4, waveFilter);
	            ps.setString(5, reviewStatusFilter);
	            ps.setInt(6, projectId);
	            ps.setString(7, fromDateFilter);
	            ps.setString(8, toDateFilter);
	            ps.setString(9, waveFilter);
	            ps.setString(10, reviewStatusFilter);
	            
	            ResultSet rs = ps.executeQuery();
	            while (rs.next()) {
	                LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
	                map.put("storeId", ifNullToEmpty(rs.getString("storeId")));
	                map.put("retailerStoreId", ifNullToEmpty(rs.getString("retailerStoreId")));
	                map.put("retailerChainCode", ifNullToEmpty(rs.getString("retailerChainCode")));
	                map.put("retailer", ifNullToEmpty(rs.getString("retailer")));
	                map.put("street", ifNullToEmpty(rs.getString("street")));
	                map.put("city", ifNullToEmpty(rs.getString("city")));
	                map.put("stateCode", ifNullToEmpty(rs.getString("stateCode")));
	                map.put("state", ifNullToEmpty(rs.getString("state")));
	                map.put("zip", ifNullToEmpty(rs.getString("zip")));
	                map.put("lat", ifNullToEmpty(rs.getString("Latitude")));
	                map.put("long", ifNullToEmpty(rs.getString("Longitude")));
	                map.put("agentId", ifNullToEmpty(rs.getString("agentId")));
	                map.put("taskId", ifNullToEmpty(rs.getString("taskId")));
                    map.put("linearFootage", rs.getString("linearFootage"));
                    map.put("countMissingUpc", rs.getString("countMissingUpc"));
                    map.put("percentageOsa", rs.getString("percentageOsa"));

	                String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
	                if ( !visitDate.isEmpty() ) {
	                	try {
							visitDate = outSdf.format(inSdf.parse(visitDate));
						} catch (ParseException e) {
                            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
						}
	                }
	                map.put("visitDate", visitDate);
	                map.put("processedDate", ifNullToEmpty(rs.getString("processedDate")));
	                map.put("resultCode", ifNullToEmpty(rs.getString("resultCode")));
	                map.put("result", ifNullToEmpty(rs.getString("criteriaName")));
	                map.put("resultColor", ifNullToEmpty(rs.getString("resultColor")));
	                map.put("resultComment", ifNullToEmpty(rs.getString("resultComment")));
	                map.put("countDistinctUpc", ifNullToEmpty(String.valueOf(rs.getInt("countDistinctUpc"))));
	                map.put("sumFacing", ifNullToEmpty(String.valueOf(rs.getInt("sumFacing"))));
	                map.put("sumUpcConfidence", ifNullToEmpty(String.valueOf(rs.getBigDecimal("sumUpcConfidence"))));
	                map.put("status", ifNullToEmpty(rs.getString("status")));
	                map.put("reviewStatus", ifNullToEmpty(rs.getString("reviewStatus")));
                    map.put("imageUUID", ifNullToEmpty(rs.getString("imageUUID")));
	                String imageUrl = rs.getString("imageURL");
	                if (imageUrl == null || imageUrl.isEmpty() ) {
	                	imageUrl = "Not Available";
	                }
	                map.put("imageURL", imageUrl );
	                result.add(map);
	            }
	            rs.close();
	            ps.close();
	            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectStoresForReview----------------\n");
	            return result;
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
	public List<Map<String, String>> getStoreLevelDistribution(int projectId, String fromDate, String toDate, String customerCode) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreLevelDistribution :: projectId={}, fromDate={}, toDate={}, customerCode={}"
				,projectId, fromDate, toDate, customerCode);
		
		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		
		String sql = "SELECT  \n" + 
				"	StoreDistribution.projectId,\n" + 
				"	StoreDistribution.storeId,\n" + 
				"	StoreDistribution.taskId, \n" + 
				"	StoreResult.visitDateId, \n" + 
				"	StoreResult.agentId,\n" + 
				"   StoreResult.customerProjectId,\n" + 
				"	SM.retailerStoreId, \n" + 
				"	SM.retailer, \n" + 
				"	SM.street, \n" + 
				"	SM.city, \n" + 
				"	SM.state, \n" + 
				"	SM.zip, \n" + 
				"	StoreDistribution.upc, \n" + 
				"	PM.brand_name, \n" + 
				"	PM.product_short_name, \n" + 
				"	StoreDistribution.facing,\n" + 
				"	COALESCE(StoreDistribution.imageUUID,'') imageUUID,\n" + 
				"	COALESCE(CustomerStoreMapping.customerStoreNumber,'') customerStoreNumber\n" + 
				"FROM \n" + 
				"	( \n" + 
				"	  SELECT  \n" + 
				"	      projectId,\n" + 
				"	      storeId,\n" + 
				"	      taskId,\n" + 
				"	      agentId,\n" + 
				"	      visitDateId,\n" + 
				"         customerProjectId\n" + 
				"	  FROM  \n" + 
				"	    ProjectStoreResult a\n" + 
				"	  WHERE  \n" + 
				"	    a.status = '1' AND a.projectId = ? AND a.visitDateId BETWEEN ? AND ?\n" + 
				"	) StoreResult\n" + 
				"	INNER JOIN \n" + 
				"	(\n" + 
				"	  SELECT\n" + 
				"	      projectId,\n" + 
				"	      storeId,\n" + 
				"	      taskId,\n" + 
				"	      upc,\n" + 
				"	      facing,\n" + 
				"	      imageUUID\n" + 
				"	  FROM\n" + 
				"	      ProjectDistributionStoreData b\n" + 
				"	  WHERE\n" + 
				"	      b.projectId = ?\n" + 
				"	) StoreDistribution \n" + 
				"	ON ( StoreDistribution.projectId = StoreResult.projectId AND StoreDistribution.storeId = StoreResult.storeId AND StoreDistribution.taskId = StoreResult.taskId)\n" + 
				"	INNER JOIN StoreMaster SM \n" + 
				"	  ON ( StoreDistribution.storeId = SM.storeId ) \n" + 
				"	INNER JOIN ProductMaster PM \n" + 
				"	  ON ( StoreDistribution.upc = PM.upc )\n" + 
				"	LEFT JOIN (\n" + 
				"	    SELECT\n" + 
				"	      storeId, customerStoreNumber\n" + 
				"	    FROM\n" + 
				"	      StoreGeoLevelMap\n" + 
				"	    WHERE\n" + 
				"	      customerCode = ?\n" + 
				"	) CustomerStoreMapping\n" + 
				"	  ON ( StoreDistribution.storeId = CustomerStoreMapping.storeId )\n" + 
				"	order by \n" + 
				"	  SM.retailerStoreId, StoreResult.taskId, StoreDistribution.upc";
		
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
		Connection conn = null;
		try {
            String fromDateParsed = inSdf.format(outSdf.parse(fromDate));
            String toDateParsed = inSdf.format(outSdf.parse(toDate));
            
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, fromDateParsed);
            ps.setString(3, toDateParsed);
            ps.setInt(4, projectId);
            ps.setString(5, customerCode);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, String> result = new LinkedHashMap<String,String>();
            	result.put("customerProjectId", ifNullToEmpty(rs.getString("customerProjectId")));
            	result.put("storeId", rs.getString("storeId"));
            	result.put("taskId", rs.getString("taskId"));
            	result.put("retailerStoreId", rs.getString("retailerStoreId"));
            	result.put("retailer", rs.getString("retailer"));
            	result.put("street", rs.getString("street"));
            	result.put("city", rs.getString("city"));
            	result.put("state", rs.getString("state"));
            	result.put("zip", rs.getString("zip"));
            	String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
            	if ( !visitDate.isEmpty() ) {
            		try {
            			visitDate = outSdf.format(inSdf.parse(visitDate));
            		} catch (ParseException e) {
            			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            		}
            	}
            	result.put("visitDate", visitDate);
            	result.put("upc", rs.getString("upc"));
            	result.put("brand_name", rs.getString("brand_name"));
            	result.put("product_short_name", rs.getString("product_short_name"));
            	result.put("facing", rs.getString("facing"));
            	result.put("imageUUID", ifNullToEmpty(rs.getString("imageUUID")));
            	result.put("customerStoreNumber", ifNullToEmpty(rs.getString("customerStoreNumber")));
            	result.put("agentId", rs.getString("agentId"));
            	resultList.add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreLevelDistribution----------------\n");
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
		return resultList;
	}
	
	@Override
	public List<Map<String, String>> getStoreImagesWithStoreMetadata(int projectId, String fromDate, String toDate, String customerCode) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getStoreImagesWithStoreMetadata :: projectId={}, fromDate={}, toDate={}, customerCode={}"
				,projectId, fromDate, toDate, customerCode);
		
		List<Map<String, String>> resultList = new ArrayList<Map<String,String>>();
		
		String sql = "SELECT  \n" + 
				"  StoreResult.projectId,\n" + 
				"  StoreResult.storeId,\n" + 
				"  StoreResult.taskId, \n" + 
				"  StoreResult.visitDateId, \n" + 
				"  StoreResult.agentId,\n" + 
				"  StoreResult.customerProjectId,\n" + 
				"  SM.retailerStoreId, \n" + 
				"  SM.retailer, \n" + 
				"  SM.street, \n" + 
				"  SM.city, \n" + 
				"  SM.state, \n" + 
				"  SM.zip, \n" + 
				"  StoreImages.imageUUID,\n" + 
				"  StoreImages.imageNotUsableComment,\n" + 
				"  COALESCE(CustomerStoreMapping.customerStoreNumber,'') customerStoreNumber\n" + 
				"FROM \n" + 
				"  ( \n" + 
				"    SELECT  \n" + 
				"        projectId,\n" + 
				"        storeId,\n" + 
				"        taskId,\n" + 
				"        agentId,\n" + 
				"        visitDateId,\n" + 
				"        customerProjectId\n" + 
				"    FROM  \n" + 
				"      ProjectStoreResult a\n" + 
				"    WHERE  \n" + 
				"      a.status = '1' AND a.projectId = ? AND a.visitDateId BETWEEN ? AND ?\n" + 
				"  ) StoreResult\n" + 
				"  INNER JOIN \n" + 
				"  (\n" + 
				"    SELECT\n" + 
				"        projectId,\n" + 
				"        storeId,\n" + 
				"        taskId,\n" + 
				"        imageUUID,\n" +
				"        imageNotUsableComment\n" + 
				"    FROM\n" + 
				"        ImageStoreNew b\n" + 
				"    WHERE\n" + 
				"        b.projectId = ?\n" + 
				"  ) StoreImages \n" + 
				"  ON ( StoreImages.projectId = StoreResult.projectId AND StoreImages.storeId = StoreResult.storeId AND StoreImages.taskId = StoreResult.taskId)\n" + 
				"  INNER JOIN StoreMaster SM \n" + 
				"    ON ( StoreImages.storeId = SM.storeId ) \n" + 
				"  LEFT JOIN (\n" + 
				"      SELECT\n" + 
				"        storeId, customerStoreNumber\n" + 
				"      FROM\n" + 
				"        StoreGeoLevelMap\n" + 
				"      WHERE\n" + 
				"        customerCode = ?\n" + 
				"  ) CustomerStoreMapping\n" + 
				"    ON ( StoreImages.storeId = CustomerStoreMapping.storeId )\n" + 
				"  order by \n" + 
				"    SM.retailerStoreId, StoreResult.taskId";
		
		SimpleDateFormat outSdf = new SimpleDateFormat("MM/dd/YYYY");
        outSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        SimpleDateFormat inSdf = new SimpleDateFormat("yyyyMMdd");
        inSdf.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
        
		Connection conn = null;
		try {
            String fromDateParsed = inSdf.format(outSdf.parse(fromDate));
            String toDateParsed = inSdf.format(outSdf.parse(toDate));
            
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, fromDateParsed);
            ps.setString(3, toDateParsed);
            ps.setInt(4, projectId);
            ps.setString(5, customerCode);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String, String> result = new LinkedHashMap<String,String>();
            	result.put("customerProjectId", ifNullToEmpty(rs.getString("customerProjectId")));
            	result.put("storeId", rs.getString("storeId"));
            	result.put("taskId", rs.getString("taskId"));
            	result.put("retailerStoreId", rs.getString("retailerStoreId"));
            	result.put("retailer", rs.getString("retailer"));
            	result.put("street", rs.getString("street"));
            	result.put("city", rs.getString("city"));
            	result.put("state", rs.getString("state"));
            	result.put("zip", rs.getString("zip"));
            	String visitDate = ifNullToEmpty(rs.getString("visitDateId"));
            	if ( !visitDate.isEmpty() ) {
            		try {
            			visitDate = outSdf.format(inSdf.parse(visitDate));
            		} catch (ParseException e) {
            			LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
            		}
            	}
            	result.put("visitDate", visitDate);
            	result.put("imageUUID", rs.getString("imageUUID"));
            	result.put("imageNotUsableComment", rs.getString("imageNotUsableComment"));
            	result.put("customerStoreNumber", ifNullToEmpty(rs.getString("customerStoreNumber")));
            	result.put("agentId", rs.getString("agentId"));
            	resultList.add(result);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreImagesWithStoreMetadata----------------\n");
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
		return resultList;
	}
	
	@Override
	public List<Map<String, String>> getHomePanelProjectStatusData() {
		
		String storeResultsSql = "select sm.retailer,sm.retailerStoreId,psr.agentId,psr.storeId,taskId from ProjectStoreResult psr,StoreMaster sm where projectId between 1383 and 1420 and taskId > '20190916' and psr.storeId=sm.storeId group by psr.storeId,psr.taskId order by psr.storeId,psr.taskId";
		String notCompletedProjectsCountRepResponseSql = "select storeId,taskId,repResponse from ProjectRepResponses where projectId=1383 and taskId > '20190916'";
		String photoCountRepResponseSql = "select pr.storeId,pr.taskId,pr.projectId,pr.repResponse from ProjectRepResponses pr, ProjectRepQuestions pq where\n" + 
				"pr.projectId between 1384 and 1420 and taskId > '20190916' and pr.projectId=pq.projectId and pr.questionId=pq.questionId and pq.questionType='PH'\n" + 
				"  order by pr.storeId,pr.taskId,pr.projectId";
		String actualPhotoCountSql="select storeId,taskId,projectId,count(*) as photoCount from ImageStoreNew where projectId between 1384 and 1420  and taskId > '20190916' group by storeId,taskId,projectId order by storeId,taskId,projectId";
    	
		Map<String,Map<String, String>> storeTaskProjectMap = new LinkedHashMap<String,Map<String,String>>();
    	Map<String,String> storeTaskNotCompletedProjectMap = new LinkedHashMap<String,String>();
    	Map<String,String> photoCountRepResponseMap = new LinkedHashMap<String,String>();
    	Map<String,String> actualPhotoCountMap = new LinkedHashMap<String,String>();

		Connection conn = null;
		try {
            
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(storeResultsSql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> oneStoreTaskProjectMap = new HashMap<String,String>();
            	oneStoreTaskProjectMap.put("retailer", rs.getString("retailer"));
            	oneStoreTaskProjectMap.put("retailerStoreId", rs.getString("retailerStoreId"));
            	oneStoreTaskProjectMap.put("agentId", rs.getString("agentId"));
            	oneStoreTaskProjectMap.put("storeId", rs.getString("storeId"));
            	oneStoreTaskProjectMap.put("taskId", rs.getString("taskId"));
            	String key = rs.getString("storeId")+"#"+rs.getString("taskId");
            	storeTaskProjectMap.put(key,oneStoreTaskProjectMap);
            }
            rs.close();
            ps.close();
            
            ps = conn.prepareStatement(notCompletedProjectsCountRepResponseSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	String key = rs.getString("storeId")+"#"+rs.getString("taskId");
            	storeTaskNotCompletedProjectMap.put(key,rs.getString("repResponse"));
            }
            rs.close();
            ps.close();
            
            ps = conn.prepareStatement(photoCountRepResponseSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	String key = rs.getString("storeId")+"#"+rs.getString("taskId")+"#"+rs.getString("projectId");
            	photoCountRepResponseMap.put(key,rs.getString("repResponse"));
            }
            rs.close();
            ps.close();
            
            ps = conn.prepareStatement(actualPhotoCountSql);
            rs = ps.executeQuery();
            while (rs.next()) {
            	String key = rs.getString("storeId")+"#"+rs.getString("taskId")+"#"+rs.getString("projectId");
            	actualPhotoCountMap.put(key,rs.getString("photoCount"));
            }
            rs.close();
            ps.close();
           
            LOGGER.info("---------------ProcessImageDaoImpl Ends getStoreImagesWithStoreMetadata----------------\n");
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
		
		List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
		for (String key : storeTaskProjectMap.keySet() ) {
			Map<String,String> storeVisitData = storeTaskProjectMap.get(key);
			String[] parts = key.split("#");
			String storeId=parts[0];
			String taskId=parts[1];
			storeVisitData.put("notCompletedProjectCount",ifNullToEmpty(storeTaskNotCompletedProjectMap.get(storeId+"#"+taskId)));
			for(int i=1384; i<1421; i++) {
				String projectKey = storeId+"#"+taskId+"#"+i;
				String reportedPhotoCount = photoCountRepResponseMap.get(projectKey);
				if (StringUtils.isBlank(reportedPhotoCount) ) { reportedPhotoCount = "0"; };
				String actualPhotoCount = actualPhotoCountMap.get(projectKey);
				if (StringUtils.isBlank(actualPhotoCount) ) { actualPhotoCount = "0"; };
				String differenceInCount = "NA";
				if (!reportedPhotoCount.equals("0") && !actualPhotoCount.equals("0") ) {
					differenceInCount = ""+ (Integer.parseInt(reportedPhotoCount) - Integer.parseInt(actualPhotoCount));
				}
				storeVisitData.put(i+"-Reported",reportedPhotoCount);
				storeVisitData.put(i+"-Actual",actualPhotoCount);
				storeVisitData.put(i+"-Difference",differenceInCount);
			}
			resultList.add(storeVisitData);
		}
		
		return resultList;
	}

	@Override
	public void updateDuplicateDetections(List<Long> duplicateDetections) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateDuplicateDetections::ids={}",
				duplicateDetections);
		
        String sql = "UPDATE ImageAnalysisNew set isDuplicate = 1 where id = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            for ( Long storeId : duplicateDetections ) {
            	ps.setLong(1, storeId);
            	ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateDuplicateDetections----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                	conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
		
	}

	@Override
	public List<Map<String, String>> getProjectPhotoCountGroups(int projectId, String limit, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectPhotoCountGroups:: projectId ={}, waveId ={}, limit ={}", projectId,waveId,limit);
        String sql = "select" + 
        		"    groupNtile, min(photoCount) as minPhotoCount, max(photoCount) as maxPhotoCount, sum(storeVisitCount) as totalStoreVisits" + 
        		" from " + 
        		" (select " + 
        		"    photoCount, count(*) as storeVisitCount, ntile(?) over (order by cast(photoCount as unsigned) asc) AS groupNtile" + 
        		" from " + 
        		" (" + 
        		"    select storeId,taskId,count(*) as photoCount from ImageStoreNew where projectId=? and (storeId,taskId) in (" +
        		"     select storeId,taskId from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? ) group by storeId,taskId order by count(*)" + 
        		" ) A" + 
        		" group by photoCount) B group by groupNtile order by groupNtile";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> groupedList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(limit));
            ps.setInt(2, projectId);
            ps.setInt(3, projectId);
            ps.setString(4, waveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> photoCountGroup = new HashMap<String,String>();
            	photoCountGroup.put("groupId", rs.getString("groupNtile"));
            	photoCountGroup.put("minPhotoCount", rs.getString("minPhotoCount"));
            	photoCountGroup.put("maxPhotoCount", rs.getString("maxPhotoCount"));
            	photoCountGroup.put("totalStoreVisits", rs.getString("totalStoreVisits"));
            	groupedList.add(photoCountGroup);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectPhotoCountGroups number of responses = {}", groupedList.size());

            return groupedList;
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
	public List<Map<String, String>> getProjectAllStorePhotoCount(int projectId, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStorePhotoCount:: projectId ={}", projectId);
        String sql = "select storeId,taskId,count(*) as photoCount from ImageStoreNew where projectId=? and (storeId,taskId) in ("
        		+ " select storeId,taskId from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? )"
        		+ " group by storeId,taskId order by count(*)";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);
            ps.setString(3, waveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> oneStoreVisit = new HashMap<String,String>();
            	oneStoreVisit.put("storeId", rs.getString("storeId"));
            	oneStoreVisit.put("taskId", rs.getString("taskId"));
            	oneStoreVisit.put("photoCount", rs.getString("photoCount"));
            	resultList.add(oneStoreVisit);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStorePhotoCount number of responses = {}", resultList.size());

            return resultList;
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
	public List<Map<String, String>> getProjectDetectedUPCCountGroups(int projectId, String limit, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectDetectedUPCCountGroups:: projectId ={}, waveId ={}, limit ={}", projectId,waveId,limit);
        String sql = "select " + 
        		"    groupNtile, min(upcCount) as minUpcCount, max(upcCount) as maxUpcCount, sum(storeVisitCount) as totalStoreVisits " + 
        		" from" + 
        		" (select " + 
        		"    upcCount, count(*) as storeVisitCount, ntile(?) over (order by cast(upcCount as unsigned) asc) AS groupNtile" + 
        		" from " + 
        		" ( " + 
        		"    select " + 
        		"            a.storeId," + 
        		"            a.taskId," + 
        		"            cast( COALESCE(b.upcCount, '0' ) as unsigned) as upcCount" + 
        		"          from" + 
        		"            (" + 
        		"                select storeId,taskId from ImageStoreNew where projectId=? and (storeId,taskId) in (" +
        		"                    select storeId,taskId from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? )" +
        		"                 group by storeId,taskId" + 
        		"            )a" + 
        		"          left join " + 
        		"            ( " + 
        		"                select storeId,taskId,count(distinct(upc)) as upcCount from ImageAnalysisNew where projectId=? group by storeId,taskId" + 
        		"            ) b" + 
        		"          ON a.storeId = b.storeId AND a.taskId = b.taskId" + 
        		" ) A" + 
        		" group by upcCount) B group by groupNtile order by groupNtile";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> groupedList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(limit));
            ps.setInt(2, projectId);
            ps.setInt(3, projectId);
            ps.setString(4, waveId);
            ps.setInt(5, projectId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> upcCountGroup = new HashMap<String,String>();
            	upcCountGroup.put("groupId", rs.getString("groupNtile"));
            	upcCountGroup.put("minUpcCount", rs.getString("minUpcCount"));
            	upcCountGroup.put("maxUpcCount", rs.getString("maxUpcCount"));
            	upcCountGroup.put("totalStoreVisits", rs.getString("totalStoreVisits"));
            	groupedList.add(upcCountGroup);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectDetectedUPCCountGroups number of responses = {}", groupedList.size());

            return groupedList;
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
	public List<Map<String, String>> getProjectAllStoreDetectedUPCCount(int projectId, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreDetectedUPCCount:: projectId ={}, waveId ={}", projectId, waveId);
        String sql = "select" + 
        		"            a.storeId," + 
        		"            a.taskId," + 
        		"            cast( COALESCE(b.upcCount, '0' ) as unsigned) as upcCount" + 
        		"          from" + 
        		"            (" + 
        		"                select storeId,taskId from ImageStoreNew where projectId=? and (storeId,taskId) in (" +
        		"                    select storeId,taskId from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? )" +
        		"                group by storeId,taskId" + 
        		"            )a" + 
        		"          left join " + 
        		"            ( " + 
        		"                select storeId,taskId,count(distinct(upc)) as upcCount from ImageAnalysisNew where projectId=? group by storeId,taskId" + 
        		"            ) b" + 
        		"          ON a.storeId = b.storeId AND a.taskId = b.taskId ORDER BY upcCount";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setInt(2, projectId);
            ps.setString(3, waveId);
            ps.setInt(4, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> oneStoreVisit = new HashMap<String,String>();
            	oneStoreVisit.put("storeId", rs.getString("storeId"));
            	oneStoreVisit.put("taskId", rs.getString("taskId"));
            	oneStoreVisit.put("upcCount", rs.getString("upcCount"));
            	resultList.add(oneStoreVisit);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreDetectedUPCCount number of responses = {}", resultList.size());

            return resultList;
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
	public List<Map<String, String>> getProjectDistributionPercentageGroups(int projectId, String limit, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectDistributionPercentageGroups:: projectId ={}, limit ={}", projectId,limit);
        String sql = "select " + 
        		"    groupNtile, min(distributionPercentage) as minDistributionPercentage, max(distributionPercentage) as maxDistributionPercentage, sum(storeVisitCount) as totalStoreVisits " + 
        		" from " + 
        		" (select " + 
        		"    distributionPercentage, count(*) as storeVisitCount, ntile(?) over (order by cast(distributionPercentage as unsigned) asc) AS groupNtile " + 
        		" from " + 
        		" (" + 
        		"    select storeId,taskId,round(coalesce(distribution,'0') * 100,0) as distributionPercentage from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? order by cast(distributionPercentage as unsigned)" + 
        		" ) A" + 
        		" group by distributionPercentage) B group by groupNtile order by groupNtile";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> groupedList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, Integer.parseInt(limit));
            ps.setInt(2, projectId);
            ps.setString(3, waveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> distPercentageGroup = new HashMap<String,String>();
            	distPercentageGroup.put("groupId", rs.getString("groupNtile"));
            	distPercentageGroup.put("minDistributionPercentage", rs.getString("minDistributionPercentage"));
            	distPercentageGroup.put("maxDistributionPercentage", rs.getString("maxDistributionPercentage"));
            	distPercentageGroup.put("totalStoreVisits", rs.getString("totalStoreVisits"));
            	groupedList.add(distPercentageGroup);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectDistributionPercentageGroups number of responses = {}", groupedList.size());

            return groupedList;
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
	public List<Map<String, String>> getProjectAllStoreDistributionPercentage(int projectId, String waveId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getProjectAllStoreDistributionPercentage:: projectId ={}, waveId ={}", projectId,waveId);
        String sql = "select storeId,taskId,round(coalesce(distribution,'0') * 100,0) as distributionPercentage  from ProjectStoreResult where projectId=? and COALESCE(waveId,'') like ? order by cast(distributionPercentage as unsigned)";
        
        if (StringUtils.isBlank(waveId) || waveId.equals("-9") ) {
        	waveId = "%";
        }
        
        Connection conn = null;
        List<Map<String,String>> resultList = new ArrayList<Map<String,String>>();
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, waveId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
            	Map<String,String> oneStoreVisit = new HashMap<String,String>();
            	oneStoreVisit.put("storeId", rs.getString("storeId"));
            	oneStoreVisit.put("taskId", rs.getString("taskId"));
            	oneStoreVisit.put("distributionPercentage", rs.getString("distributionPercentage"));
            	resultList.add(oneStoreVisit);
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends getProjectAllStoreDistributionPercentage number of responses = {}", resultList.size());

            return resultList;
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
	public Map<String, List<String>> getImages(String projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getImages::projectId={},storeId={},taskId={}",projectId,storeId,taskId);
        String sql = "SELECT storeId,taskId,imageUUID FROM ImageStoreNew WHERE projectId = ?";
        
        boolean specificStoreVisit = false;
        if( ! "all".equals(storeId) ) {
        	specificStoreVisit = true;
        	sql = sql + " and storeId = ? and taskid = ?";
        }
        
        Connection conn = null;
        
        Map<String,List<String>> storeImages = new HashMap<String,List<String>>();
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, projectId);
            if ( specificStoreVisit ) {
            	 ps.setString(2, storeId);
            	 ps.setString(3, taskId);
            }
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String internalStoreId = rs.getString("storeId");
                if ( storeImages.get(internalStoreId) == null ) {
                	storeImages.put(internalStoreId,new ArrayList<String>());
                }
                storeImages.get(internalStoreId).add(rs.getString("imageUUID"));
            }
            rs.close();
            ps.close();
            
            LOGGER.info("---------------ProcessImageDaoImpl Ends getImages:: storewiseImages = {}",storeImages);

            return storeImages;
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
    public List<ImageStore> getImagesForExternalProcessing(int projectId, String limit) {
        LOGGER.info("---------------ProcessImageDaoImpl Starts getImagesForExternalProcessing::projectId={},limit={}----------------\n",projectId,limit);
        String sql = "SELECT storeId,taskId,imageUUID,categoryId FROM ImageStoreNew "
        		+ " WHERE projectId = ? AND imageStatus = ? AND questionId in ( SELECT questionId FROM ProjectRepQuestions WHERE projectId = ? AND skipImageAnalysis = '0' )"
        		+ " ORDER BY lastUpdatedTimestamp LIMIT ?";
        Connection conn = null;
        List<ImageStore> images = new ArrayList<ImageStore>();
        try {
            conn = dataSource.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId); 
            ps.setString(2, Constants.EXTERNAL_PROCESSING_IMAGE_STATUS);
            ps.setInt(3, projectId); 
            ps.setInt(4, Integer.parseInt(limit));

            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
            	ImageStore oneImage = new ImageStore();
            	oneImage.setStoreId(rs.getString("storeId"));
            	oneImage.setTaskId(rs.getString("taskId"));
            	oneImage.setImageUUID(rs.getString("imageUUID"));
            	oneImage.setCategoryId(rs.getString("categoryId"));

                images.add(oneImage);
            }
            rs.close();
            ps.close();

            LOGGER.info("---------------ProcessImageDaoImpl End getImagesForExternalProcessing----------------\n");

            return images;
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
	public boolean isProcessingComplete(int projectId, String storeId, String taskId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts isProcessingComplete::projectId={}::storeId={}::taskId={}", projectId,storeId,taskId);
        String sql = "SELECT DISTINCT(imageStatus) FROM ImageStoreNew WHERE projectId=? AND storeId=? AND taskId=?";
        boolean isProcessingComplete = true;
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, projectId);
            ps.setString(2, storeId);
            ps.setString(3,taskId);

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                String imageStatus = rs.getString("imageStatus");
                if ( !imageStatus.equals("done") && !imageStatus.equals("error") ) {
                	isProcessingComplete = false;
                	break;
                }
            }
            rs.close();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends isProcessingComplete::value= {}",isProcessingComplete);

            return isProcessingComplete;
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
	public void updateShelfLevelAndDuplicateDetections(List<String> updateList) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts updateShelfLevelAndDuplicateDetections::size={}",
				updateList.size());

        String sql = "UPDATE ImageAnalysisNew set isDuplicate = ?, shelfLevel = ? where id = ?";
        Connection conn = null;

        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            PreparedStatement ps = conn.prepareStatement(sql);
            for ( String oneDetection : updateList ) {
            	String[] parts = oneDetection.split("#");
            	ps.setString(1, parts[2]);
            	ps.setString(2, parts[1]);
            	ps.setString(3, parts[0]);
            	ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
            LOGGER.info("---------------ProcessImageDaoImpl Ends updateShelfLevelAndDuplicateDetections----------------\n");
        } catch (SQLException e) {
            LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                	conn.close();
                } catch (SQLException e) {
                    LOGGER.error("EXCEPTION {} , {}", e.getMessage(), e);
                }
            }
        }
	}

	@Override
	public LinkedHashMap<String, Object> getPhotoQualityMetricsByUserId(String userId) {
		LOGGER.info("---------------ProcessImageDaoImpl Starts getPhotoQualityMetricsByUserId::userId={}",userId);
        String totalPhotoCountSql = "SELECT \n" + 
        		"    COUNT(*) AS totalPhotoCount \n" + 
        		"FROM ImageStoreNew isn, ProjectStoreResult psr \n" + 
        		"WHERE isn.agentId=? AND isn.dateId >= ? \n" + 
        		"    AND isn.projectId = psr.projectId AND isn.storeId = psr.storeId AND isn.taskId = psr.taskId\n" + 
        		"    AND psr.resultCode NOT IN (99,998,999)";
        
        String poorPhotoDetailsSql = "SELECT \n" + 
        		"    isn.projectId,isn.imageUUID,isn.imagenotusablecomment  \n" + 
        		"FROM  \n" + 
        		"    ImageStoreNew isn, ProjectStoreResult psr\n" + 
        		"WHERE isn.agentId=? AND isn.dateId >= ? AND isn.imagenotusable='1'\n" + 
        		"    AND isn.projectId = psr.projectId AND isn.storeId = psr.storeId AND isn.taskId = psr.taskId\n" + 
        		"    AND psr.resultCode NOT IN (99,998,999)";
        
        Connection conn = null;
        
        String dateId = LocalDate.now().minusDays(30).format(DateTimeFormatter.BASIC_ISO_DATE);
        
        LinkedHashMap<String,Object> metrics = new LinkedHashMap<String,Object>();
        
        int totalPhotoCount = 0;
        
        try {
            conn = dataSource.getConnection();
            PreparedStatement ps = conn.prepareStatement(totalPhotoCountSql);
            ps.setString(1, userId);
            ps.setString(2, dateId);
            
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                totalPhotoCount = rs.getInt("totalPhotoCount");
            }
            rs.close();
            ps.close();
            
            metrics.put("totalPhotoCount", totalPhotoCount);
            
            List<Map<String,Object>> issues = new ArrayList<Map<String,Object>>();
            
            int poorPhotoCount = 0;
            if ( totalPhotoCount > 0 ) {
            	PreparedStatement detailsPs = conn.prepareStatement(poorPhotoDetailsSql);
            	detailsPs.setString(1, userId);
            	detailsPs.setString(2, dateId);
                
            	Map<String,List<String>> issuePhotoMap = new TreeMap<String,List<String>>();
                ResultSet detailsRs = detailsPs.executeQuery();
                while (detailsRs.next()) {
                    int projectId = detailsRs.getInt("projectId");
                    String imageUUID = detailsRs.getString("imageUUID");
                    String imageNotUsableComment = detailsRs.getString("imagenotusablecomment");
                    String[] parts = imageNotUsableComment.split(",");
                    for(String part : parts) {
                    	if ( issuePhotoMap.get(part) == null ) {
                    		issuePhotoMap.put(part,new ArrayList<String>());
                    	}
                    	issuePhotoMap.get(part).add(projectId+"/"+imageUUID);
                    }
                }
                detailsRs.close();
                detailsPs.close();
                
                for( String issueType : issuePhotoMap.keySet() ) {
                	List<String> photoList = issuePhotoMap.get(issueType);
                	Map<String,Object> oneIssue = new HashMap<String,Object>();
                	List<String> issueTypeMetaDetails = IMAGE_QUALITY_ISSUE_TYPE_MAP.get(issueType);
                	if ( issueTypeMetaDetails != null ) {
                		oneIssue.put("issueType", issueTypeMetaDetails.get(0) );
                		oneIssue.put("issueTypeColor", issueTypeMetaDetails.get(1) );
                		oneIssue.put("photoCount", photoList.size());
                		oneIssue.put("imageList", photoList);
                		issues.add(oneIssue);
                		
                		poorPhotoCount = poorPhotoCount + photoList.size();
                		
                	}
                }
            } 
            
            metrics.put("poorQualityPhotoCount", poorPhotoCount);
            metrics.put("qualityIssues", issues);
            
            LOGGER.info("---------------ProcessImageDaoImpl Ends getPhotoQualityMetricsByUserId:: metrics = {}",metrics);

            return metrics;
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
}
