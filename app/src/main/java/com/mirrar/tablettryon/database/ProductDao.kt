package com.mirrar.tablettryon.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.mirrar.tablettryon.products.model.product.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {


    @Query("SELECT * FROM product_table ORDER BY name ASC")
    fun getProductsSortedAsc(): Flow<List<Product>>

    @Query("SELECT * FROM product_table ORDER BY name DESC")
    fun getProductsSortedDesc(): Flow<List<Product>>

    @Query(
        """
        SELECT * FROM product_table
        WHERE (:name IS NULL OR name LIKE '%' || :name || '%')
        AND (:objectId IS NULL OR objectId = :objectId)
        AND (:brand IS NULL OR brand LIKE '%' || :brand || '%')
        """
    )
    fun findProducts(name: String?, objectId: String?, brand: String?): Flow<List<Product>>

    @Query(
        """
        SELECT * FROM product_table
        WHERE name LIKE '%' || :searchText || '%'
           OR objectId LIKE '%' || :searchText || '%'
           OR brand LIKE '%' || :searchText || '%'
        """
    )
    fun searchProducts(searchText: String): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateProduct(product: Product)

    @Upsert
    suspend fun upsertProduct(product: Product)

    @Query("SELECT * FROM product_table WHERE name LIKE '%' || :name || '%'")
    fun getProductsByName(name: String): Flow<List<Product>>

    @Query("DELETE FROM product_table")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM product_table")
    suspend fun getCount(): Int
}