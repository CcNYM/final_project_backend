package com.training.backend.dao;

import com.training.backend.entity.UserPosition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserPositionMapper {

    public int updateUserPositionByUserPosition(UserPosition userPosition);

    public UserPosition selectUserPositionByUserIdAndStockId(@Param("userId")int userId, @Param("stockId")int stockId);

    public int deleteUserPositionByUserIdAndStockId(@Param("userId")int userId,@Param("stockId")int stockId);

    public int insertUserPosition(UserPosition userPosition);

    public List<UserPosition> selectUserPositionListByUserId(int userId);
}
