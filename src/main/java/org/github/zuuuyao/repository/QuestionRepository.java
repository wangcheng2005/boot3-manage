package org.github.zuuuyao.repository;

import org.github.zuuuyao.config.mybatis.extension.BaseMapperExtension;
import org.github.zuuuyao.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends BaseMapperExtension<QuestionEntity> {
}

