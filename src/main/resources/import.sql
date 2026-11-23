insert into categoria (nome, descricao) values ('Decoração', 'Vasos, luminárias e peças para enfeitar a casa');
insert into categoria (nome, descricao) values ('Utilidades', 'Organizadores, suportes e objetos do dia a dia');
insert into categoria (nome, descricao) values ('Brinquedos e colecionáveis', 'Miniaturas, action figures e jogos');
insert into categoria (nome, descricao) values ('Peças técnicas', 'Engrenagens, peças de reposição e protótipos');

insert into usuario (nome, email, telefone, tipo, ativo, criado_em) values ('Carlos Mendes', 'carlos@printhub.local', '14999990001', 'PRODUTOR', true, current_timestamp);
insert into usuario (nome, email, telefone, tipo, ativo, criado_em) values ('Beatriz Rocha', 'beatriz@printhub.local', '14999990002', 'PRODUTOR', true, current_timestamp);
insert into usuario (nome, email, telefone, tipo, ativo, criado_em) values ('João Pereira', 'joao@printhub.local', '14999990003', 'CONSUMIDOR', true, current_timestamp);
insert into usuario (nome, email, telefone, tipo, ativo, criado_em) values ('Maria Souza', 'maria@printhub.local', '14999990004', 'CONSUMIDOR', true, current_timestamp);
insert into usuario (nome, email, telefone, tipo, ativo, criado_em) values ('Pedro Lima', 'pedro@printhub.local', '14999990005', 'CONSUMIDOR', true, current_timestamp);

insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Vaso geométrico', 'Vaso facetado de 15 cm para suculentas', 'PLA', 'Branco', 48.00, 10, 3, true, 1, 1, current_timestamp);
insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Suporte para celular', 'Suporte de mesa com ângulo ajustável', 'PLA', 'Preto', 25.00, 15, 2, true, 2, 1, current_timestamp);
insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Organizador de mesa', 'Organizador com 4 divisórias para canetas e clipes', 'PETG', 'Cinza', 39.90, 8, 3, true, 2, 1, current_timestamp);
insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Miniatura de dragão', 'Miniatura pintada à mão, 12 cm de altura', 'Resina', 'Verde', 79.90, 2, 5, true, 3, 2, current_timestamp);
insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Engrenagem de reposição', 'Engrenagem de 32 dentes para batedeira', 'ABS', 'Preto', 15.50, 30, 1, true, 4, 2, current_timestamp);
insert into produto (nome, descricao, material, cor, preco, estoque, dias_producao, ativo, categoria_id, produtor_id, criado_em) values ('Luminária lua', 'Luminária em formato de lua com textura de crateras', 'PLA', 'Branco', 120.00, 3, 4, true, 1, 2, current_timestamp);

insert into ponto_retirada (nome, endereco, bairro, cidade, custo_entrega, ativo) values ('Ponto Centro', 'Rua Paraná, 500', 'Centro', 'Ourinhos', 20.00, true);
insert into ponto_retirada (nome, endereco, bairro, cidade, custo_entrega, ativo) values ('Ponto Vila Margarida', 'Avenida Jacinto Ferreira de Sá, 1200', 'Vila Margarida', 'Ourinhos', 15.00, true);
insert into ponto_retirada (nome, endereco, bairro, cidade, custo_entrega, ativo) values ('Ponto Jardim Paulista', 'Rua Expedicionários, 80', 'Jardim Paulista', 'Ourinhos', 12.00, true);
