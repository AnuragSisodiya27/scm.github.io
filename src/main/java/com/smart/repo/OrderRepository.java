package com.smart.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smart.entity.MyOrder;

public interface OrderRepository extends JpaRepository<MyOrder,Long>{

	public MyOrder findByOrderId(String orderId);
}
