package me.moonsoo.travelerrestapi.post;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;

import static me.moonsoo.travelerrestapi.post.QPost.post;
import static me.moonsoo.travelerrestapi.post.QPostImage.postImage;
import static me.moonsoo.travelerrestapi.post.QPostTag.*;

public class PostCustomRepositoryImpl extends QuerydslRepositorySupport implements PostCustomRepository {

    @Autowired
    private JPAQueryFactory queryFactory;

    public PostCustomRepositoryImpl() {
        super(Post.class);
    }

    @Override
    public Page<Post> findAllByTagContains(String search, Pageable pageable) {
        BooleanExpression predicate = postTag.tag.containsIgnoreCase(search);
        JPAQuery<Post> query = queryFactory.selectFrom(post)
                .leftJoin(postTag).on(post.id.eq(postTag.post.id))
                .where(predicate);

        List<Post> posts = getQuerydsl().applyPagination(pageable, query).fetch();
        return new PageImpl(posts, pageable, query.fetchCount());
    }
}
