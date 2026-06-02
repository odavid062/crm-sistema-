package com.crm.channel;

import com.crm.channel.dto.ChannelRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    public Channel findById(UUID id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Canal não encontrado"));
    }

    @Transactional
    public Channel create(ChannelRequest request) {
        Channel channel = Channel.builder()
                .name(request.name())
                .type(request.type())
                .config(request.config())
                .isDefault(Boolean.TRUE.equals(request.isDefault()))
                .status(ChannelStatus.DISCONNECTED)
                .build();
        return channelRepository.save(channel);
    }

    @Transactional
    public Channel update(UUID id, ChannelRequest request) {
        Channel channel = findById(id);
        if (request.name() != null) channel.setName(request.name());
        if (request.config() != null) channel.setConfig(request.config());
        if (request.isDefault() != null) channel.setDefault(request.isDefault());
        return channelRepository.save(channel);
    }

    @Transactional
    public Channel changeStatus(UUID id, ChannelStatus status) {
        Channel channel = findById(id);
        channel.setStatus(status);
        return channelRepository.save(channel);
    }

    @Transactional
    public void delete(UUID id) {
        channelRepository.deleteById(id);
    }
}
