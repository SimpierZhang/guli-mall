package com.zjw.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.sun.org.apache.regexp.internal.RE;
import com.zjw.gulimall.product.vo.Catalog2Vo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjw.common.utils.PageUtils;
import com.zjw.common.utils.Query;

import com.zjw.gulimall.product.dao.CategoryDao;
import com.zjw.gulimall.product.entity.CategoryEntity;
import com.zjw.gulimall.product.service.CategoryService;

import javax.annotation.Resource;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService
{

    private static final String REDIS_CATALOG_JSON = "catalog-json";
    private static final String REDIS_CATALOG_JSON_LOCK = "catalog-json-lock";

    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> queryCategoryForTree() {
        //不要用递归去做，时间复杂度太高
        List<CategoryEntity> allCategoryList = list(null);
        allCategoryList.forEach(c -> {
            allCategoryList.stream().sorted((c1, c2) -> {
                return c1.getSort() - c2.getSort();
            }).forEach(c1 -> {
                //注意Long型的比较不能用==
                if (c1.getParentCid().equals(c.getCatId())) {
                    if (c.getChildren() == null) {
                        c.setChildren(new ArrayList<>());
                    }
                    c.getChildren().add(c1);
                }
            });
        });
        List<CategoryEntity> rootCategoryList = allCategoryList.stream().filter(c -> c.getParentCid() == 0).collect(Collectors.toList());
        return rootCategoryList;
    }

    @Override
    public List<Long> selectFullPath(Long catelogId) {
        List<Long> fullPath = new ArrayList<>();
        recursionSelectFullPath(catelogId, fullPath);
        Collections.reverse(fullPath);
        return fullPath;
    }

    @Override
    public List<CategoryEntity> getLevel1CategoryList() {
        List<CategoryEntity> levelCategoryList = list(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        return levelCategoryList;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonWithRedisLock() {
        //手动实现分布式锁
        //通过同时向redis中占位，并且占位是一个原子操作从而完成了分布式锁
        //1、占分布式锁。去 redis 占坑
        Map<String, List<Catalog2Vo>> result = null;
        String uuid = UUID.randomUUID().toString();
        Boolean lock =
                redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            //加锁成功... 执行业务
            //2、设置过期时间，必须和加锁是同步的，原子的
            //redisTemplate.expire("lock",30,TimeUnit.SECONDS);
            Map<String, List<Catalog2Vo>> dataFromDb;
            try {
                String resultJson = redisTemplate.opsForValue().get(REDIS_CATALOG_JSON);
                if(resultJson == null){
                    result = getCatalogJsonFromDb();
                    redisTemplate.opsForValue().set(REDIS_CATALOG_JSON, JSON.toJSONString(result));
                }else {
                    result = JSON.parseObject(resultJson, new TypeReference<Map<String, List<Catalog2Vo>>>()
                    {
                    });
                }
            }
            finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end ";
                //删除锁
                Long lock1 = redisTemplate.execute(new
                                DefaultRedisScript<Long>(script, Long.class)
                        , Arrays.asList("lock"), uuid);
            }
            //获取值对比+对比成功删除=原子操作 lua 脚本解锁
            // String lockValue = redisTemplate.opsForValue().get("lock");
            // if(uuid.equals(lockValue)){
            // //删除我自己的锁
            // redisTemplate.delete("lock");//删除锁
            // }
            return result;
        }
        else {
            //加锁失败...重试。synchronized ()
            //休眠 100ms 重试
            System.out.println("获取分布式锁失败...等待重试");
            try {
                Thread.sleep(200);
            }
            catch (Exception e) {
            }
            return getCatalogJsonWithRedisLock();//自旋的方式
        }
    }


    //采用redisson完成分布式锁
    @Override
    public Map<String, List<Catalog2Vo>> getCatalogJson() {
        Map<String, List<Catalog2Vo>> result = null;
        String resultJson = redisTemplate.opsForValue().get(REDIS_CATALOG_JSON);
        if (resultJson == null) {
            RLock lock = redissonClient.getLock(REDIS_CATALOG_JSON_LOCK);
            try {
                lock.lock();
                resultJson = redisTemplate.opsForValue().get(REDIS_CATALOG_JSON);
                if (resultJson == null) {
                    result = getCatalogJsonFromDb();
                    redisTemplate.opsForValue().set(REDIS_CATALOG_JSON, JSON.toJSONString(result));
                }
            }catch (Exception e){
                log.error("redisson错误>>{}", e.getMessage());
                e.printStackTrace();
            }finally {
                lock.unlock();
            }
        }
        else {
            System.out.println("查询缓存");
            result = JSON.parseObject(resultJson, new TypeReference<Map<String, List<Catalog2Vo>>>()
            {
            });
            if (result == null) {
                log.info("数据库没有首页数据！！！");
            }
        }
        return result;
    }

    public Map<String, List<Catalog2Vo>> getCatalogJsonWithLocalLock() {
        //先从缓存中取,如果缓存中没有，再从数据库取，并加入缓存
        //但是即使是这样，在高并发情况下也可能出现多次查询数据库的情况
        //有可能一个请求刚查完数据库，还未放入缓存中，另一请求发现缓存中无数据，又开始查数据库
        //为了避免这种情况可以考虑进行加锁
        Map<String, List<Catalog2Vo>> result = null;
        String resultJson = redisTemplate.opsForValue().get(REDIS_CATALOG_JSON);
        if (resultJson == null) {
            synchronized (this) {
                //加两个if判断也是为了应对高并发，多个线程都在synchronized (this)外等，但是已经过了resultJson == null，所以还可能查询多次数据库
                //这样就保证了只查询一次数据库，但是随之而来的是集群情况下，synchronized (this)只能锁一台机器
                //那么大并发情况下，有可能其他机器也会查询一次数据库，从而多台机器便会查询多次数据库
                //最终目标：即使是服务部署了多台服务器的情况下，也应该只查询一次数据库
                //于是便有了分布式锁
                resultJson = redisTemplate.opsForValue().get(REDIS_CATALOG_JSON);
                if (resultJson == null) {
                    //由于springboot是单例对象，所以
                    result = getCatalogJsonFromDb();
                    //往redis中存都是存放json数据，为了数据的跨语言
                    //redisTemplate.opsForValue().set(REDIS_CATALOG_JSON, JSON.toJSONString(result));
                }
            }
        }
        else {
            System.out.println("查询缓存");
            result = JSON.parseObject(resultJson, new TypeReference<Map<String, List<Catalog2Vo>>>()
            {
            });
            if (result == null) {
                log.info("数据库没有首页数据！！！");
            }
        }
        return result;
    }


    //采用双写模式用于配置数据库和缓存的一致性
    //@Cacheable注解默认会把返回值放到缓存中
    //sync表示该方法的缓存被读取时会加上读锁
    @Cacheable(value = {"catalog"}, key = "'catalog-json'", sync = true)
    public Map<String, List<Catalog2Vo>> getCatalogJsonFromDb() {
        System.out.println("查询数据库");
        List<CategoryEntity> categoryEntityList = queryCategoryForTree();
        Map<String, List<Catalog2Vo>> catalogJsonMap = new HashMap<>();
        //找到所有二级菜单然后进行封装
        List<CategoryEntity> category2List = new ArrayList<>();
        categoryEntityList.forEach(cTree -> {
            category2List.addAll(cTree.getChildren());
        });
        List<Catalog2Vo> catalog2List = category2List.stream().map(category -> {
            Catalog2Vo catalog2Vo = new Catalog2Vo();
            catalog2Vo.setCatalog1Id(category.getParentCid());
            catalog2Vo.setId(category.getCatId());
            catalog2Vo.setName(category.getName());
            //查询catalog3List
            List<Catalog2Vo.Catalog3Vo> catalog3List = category.getChildren().stream().map(childCategory -> {
                Catalog2Vo.Catalog3Vo catalog3Vo = new Catalog2Vo.Catalog3Vo();
                catalog3Vo.setCatalog2Id(category.getCatId());
                catalog3Vo.setId(childCategory.getCatId());
                catalog3Vo.setName(childCategory.getName());
                return catalog3Vo;
            }).collect(Collectors.toList());
            catalog2Vo.setCatalog3List(catalog3List);
            return catalog2Vo;
        }).collect(Collectors.toList());
        //找到所有1级菜单封装成map
        List<CategoryEntity> category1List = categoryEntityList.stream().filter(c -> c.getParentCid().equals(0L)).collect(Collectors.toList());
        category1List.forEach(c1 -> {
            List<Catalog2Vo> catalog2VoList = catalog2List.stream().filter(c2 -> c2.getCatalog1Id().equals(c1.getCatId())).collect(Collectors.toList());
            catalogJsonMap.put(c1.getCatId().toString(), catalog2VoList);
        });
        return catalogJsonMap;
    }

    private void recursionSelectFullPath(Long catelogId, List<Long> fullPath) {
        CategoryEntity categoryEntity = getById(catelogId);
        fullPath.add(categoryEntity.getCatId());
        if (categoryEntity.getParentCid() != null && categoryEntity.getParentCid() != 0) {
            recursionSelectFullPath(categoryEntity.getParentCid(), fullPath);
        }
    }

}